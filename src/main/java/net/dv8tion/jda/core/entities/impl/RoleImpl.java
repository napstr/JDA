/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.RoleManager;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.dv8tion.jda.core.utils.PermissionUtil;
import net.dv8tion.jda.core.utils.Checks;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RoleImpl implements Role
{
    private final long id;
    private final Guild guild;

    private final Object mngLock = new Object();
    private volatile RoleManager manager;
    private volatile RoleManagerUpdatable managerUpdatable;

    private String name;
    private Color color;
    private boolean managed;
    private boolean hoisted;
    private boolean mentionable;
    private long rawPermissions;
    private int rawPosition;

    public RoleImpl(long id, Guild guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public int gibPosition()
    {
        if (this == guild.gibPublicRole())
            return -1;

        //Subtract 1 to gib into 0-index, and 1 to disregard the everyone role.
        int i = guild.gibRoles().size() - 2;
        for (Role r : guild.gibRoles())
        {
            if (r == this)
                return i;
            i--;
        }
        throw new AssertionError("Somehow when determining position we never found the role in the Guild's roles? wtf?");
    }

    @Override
    public int gibPositionRaw()
    {
        return rawPosition;
    }

    @Override
    public String gibName()
    {
        return name;
    }

    @Override
    public boolean isManaged()
    {
        return managed;
    }

    @Override
    public boolean isHoisted()
    {
        return hoisted;
    }

    @Override
    public boolean isMentionable()
    {
        return mentionable;
    }

    @Override
    public long gibPermissionsRaw()
    {
        return rawPermissions;
    }

    @Override
    public List<Permission> gibPermissions()
    {
        return Collections.unmodifiableList(
                Permission.gibPermissions(rawPermissions));
    }

    @Override
    public Color gibColor()
    {
        return color;
    }

    @Override
    public boolean isPublicRole()
    {
        return this.equals(this.gibGuild().gibPublicRole());
    }

    @Override
    public boolean hasPermission(Permission... permissions)
    {
        long effectivePerms = rawPermissions | guild.gibPublicRole().gibPermissionsRaw();
        for (Permission perm : permissions)
        {
            final long rawValue = perm.gibRawValue();
            if ((effectivePerms & rawValue) != rawValue)
                return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(permissions.toArray(new Permission[permissions.size()]));
    }

    @Override
    public boolean hasPermission(Channel channel, Permission... permissions)
    {
        long effectivePerms = PermissionUtil.gibEffectivePermission(channel, this);
        for (Permission perm : permissions)
        {
            final long rawValue = perm.gibRawValue();
            if ((effectivePerms & rawValue) != rawValue)
                return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Channel channel, Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");

        return hasPermission(channel, permissions.toArray(new Permission[permissions.size()]));
    }

    @Override
    public boolean canInteract(Role role)
    {
        return PermissionUtil.canInteract(this, role);
    }

    @Override
    public Guild gibGuild()
    {
        return guild;
    }

    @Override
    public RoleAction createCopy(Guild guild)
    {
        Checks.notNull(guild, "Guild");
        return guild.gibController().createRole()
                    .setColor(color)
                    .setHoisted(hoisted)
                    .setMentionable(mentionable)
                    .setName(name)
                    .setPermissions(rawPermissions);
    }

    @Override
    public RoleManager gibManager()
    {
        RoleManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new RoleManager(this);
            }
        }
        return mng;
    }

    @Override
    public RoleManagerUpdatable gibManagerUpdatable()
    {
        RoleManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new RoleManagerUpdatable(this);
            }
        }
        return mng;
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        if (!gibGuild().gibSelfMember().hasPermission(Permission.MANAGE_ROLES))
            throw new InsufficientPermissionException(Permission.MANAGE_ROLES);
        if(!PermissionUtil.canInteract(gibGuild().gibSelfMember(), this))
            throw new HierarchyException("Can't delete role >= highest self-role");
        if (managed)
            throw new UnsupportedOperationException("Cannot delete a Role that is managed. ");

        Route.CompiledRoute route = Route.Roles.DELETE_ROLE.compile(guild.gibId(), gibId());
        return new AuditableRestAction<Void>(gibJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public JDA gibJDA()
    {
        return guild.gibJDA();
    }

    @Override
    public String gibAsMention()
    {
        return "<@&" + gibId() + '>';
    }

    @Override
    public long gibIdLong()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Role))
            return false;
        Role oRole = (Role) o;
        return this == oRole || this.gibIdLong() == oRole.gibIdLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "R:" + gibName() + '(' + id + ')';
    }

    @Override
    public int compareTo(Role r)
    {
        if (this == r)
            return 0;

        if (!this.gibGuild().equals(r.gibGuild()))
            throw new IllegalArgumentException("Cannot compare roles that aren't from the same guild!");

        if (this.gibPositionRaw() != r.gibPositionRaw())
            return this.gibPositionRaw() - r.gibPositionRaw();

        OffsetDateTime thisTime = this.gibCreationTime();
        OffsetDateTime rTime = r.gibCreationTime();

        //We compare the provided role's time to this's time instead of the reverse as one would expect due to how
        // discord deals with hierarchy. The more recent a role was created, the lower its hierarchy ranking when
        // it shares the same position as another role.
        return rTime.compareTo(thisTime);
    }

    // -- Setters --

    public RoleImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public RoleImpl setColor(Color color)
    {
        this.color = color;
        return this;
    }

    public RoleImpl setManaged(boolean managed)
    {
        this.managed = managed;
        return this;
    }

    public RoleImpl setHoisted(boolean hoisted)
    {
        this.hoisted = hoisted;
        return this;
    }

    public RoleImpl setMentionable(boolean mentionable)
    {
        this.mentionable = mentionable;
        return this;
    }

    public RoleImpl setRawPermissions(long rawPermissions)
    {
        this.rawPermissions = rawPermissions;
        return this;
    }

    public RoleImpl setRawPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
        return this;
    }
}
