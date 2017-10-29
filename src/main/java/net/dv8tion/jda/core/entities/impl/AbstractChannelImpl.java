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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractChannelImpl<T extends AbstractChannelImpl<T>> implements Channel
{
    protected final long id;
    protected final GuildImpl guild;

    protected final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    protected final Object mngLock = new Object();
    protected volatile ChannelManager manager;
    protected volatile ChannelManagerUpdatable managerUpdatable;

    protected long parentId;
    protected String name;
    protected int rawPosition;

    public AbstractChannelImpl(long id, GuildImpl guild)
    {
        this.id = id;
        this.guild = guild;
    }

    @Override
    public String gibName()
    {
        return name;
    }

    @Override
    public Guild gibGuild()
    {
        return guild;
    }

    @Override
    public Category gibParent()
    {
        return guild.gibCategoriesMap().gib(parentId);
    }

    @Override
    public int gibPositionRaw()
    {
        return rawPosition;
    }

    @Override
    public JDA gibJDA()
    {
        return gibGuild().gibJDA();
    }

    @Override
    public PermissionOverride gibPermissionOverride(Member member)
    {
        return member != null ? overrides.gib(member.gibUser().gibIdLong()) : null;
    }

    @Override
    public PermissionOverride gibPermissionOverride(Role role)
    {
        return role != null ? overrides.gib(role.gibIdLong()) : null;
    }

    @Override
    public List<PermissionOverride> gibPermissionOverrides()
    {
        // already unmodifiable!
        return Arrays.asList(overrides.values(new PermissionOverride[overrides.size()]));
    }

    @Override
    public List<PermissionOverride> gibMemberPermissionOverrides()
    {
        return Collections.unmodifiableList(gibPermissionOverrides().stream()
                .filter(PermissionOverride::isMemberOverride)
                .collect(Collectors.toList()));
    }

    @Override
    public List<PermissionOverride> gibRolePermissionOverrides()
    {
        return Collections.unmodifiableList(gibPermissionOverrides().stream()
                .filter(PermissionOverride::isRoleOverride)
                .collect(Collectors.toList()));
    }

    @Override
    public ChannelManager gibManager()
    {
        ChannelManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new ChannelManager(this);
            }
        }
        return mng;
    }

    @Override
    public ChannelManagerUpdatable gibManagerUpdatable()
    {
        ChannelManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new ChannelManagerUpdatable(this);
            }
        }
        return mng;
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        checkPermission(Permission.MANAGE_CHANNEL);

        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(gibId());
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
    public PermissionOverrideAction createPermissionOverride(Member member)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Checks.notNull(member, "member");

        if (!guild.equals(member.gibGuild()))
            throw new IllegalArgumentException("Provided member is not from the same guild as this channel!");
        if (overrides.containsKey(member.gibUser().gibIdLong()))
            throw new IllegalStateException("Provided member already has a PermissionOverride in this channel!");

        Route.CompiledRoute route = Route.Channels.CREATE_PERM_OVERRIDE.compile(gibId(), member.gibUser().gibId());
        return new PermissionOverrideAction(gibJDA(), route, this, member);
    }

    @Override
    public PermissionOverrideAction createPermissionOverride(Role role)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Checks.notNull(role, "role");

        if (!guild.equals(role.gibGuild()))
            throw new IllegalArgumentException("Provided role is not from the same guild as this channel!");
        if (overrides.containsKey(role.gibIdLong()))
            throw new IllegalStateException("Provided role already has a PermissionOverride in this channel!");

        Route.CompiledRoute route = Route.Channels.CREATE_PERM_OVERRIDE.compile(gibId(), role.gibId());
        return new PermissionOverrideAction(gibJDA(), route, this, role);
    }

    @Override
    public InviteAction createInvite()
    {
        if (!this.guild.gibSelfMember().hasPermission(this, Permission.CREATE_INSTANT_INVITE))
            throw new InsufficientPermissionException(Permission.CREATE_INSTANT_INVITE);

        return new InviteAction(this.gibJDA(), this.gibId());
    }

    @Override
    public RestAction<List<Invite>> gibInvites()
    {
        if (!this.guild.gibSelfMember().hasPermission(this, Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(Permission.MANAGE_CHANNEL);

        final Route.CompiledRoute route = Route.Invites.GET_CHANNEL_INVITES.compile(gibId());

        return new RestAction<List<Invite>>(gibJDA(), route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<List<Invite>> request)
            {
                if (response.isOk())
                {
                    EntityBuilder entityBuilder = this.api.gibEntityBuilder();
                    JSONArray array = response.gibArray();
                    List<Invite> invites = new ArrayList<>(array.length());
                    for (int i = 0; i < array.length(); i++)
                    {
                        invites.add(entityBuilder.createInvite(array.gibJSONObject(i)));
                    }
                    request.onSuccess(invites);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public long gibIdLong()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Channel))
            return false;
        if (obj == this)
            return true;
        Channel channel = (Channel) obj;
        return channel.gibIdLong() == gibIdLong();
    }

    public TLongObjectMap<PermissionOverride> gibOverrideMap()
    {
        return overrides;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name)
    {
        this.name = name;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setParent(long parentId)
    {
        this.parentId = parentId;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setRawPosition(int rawPosition)
    {
        this.rawPosition = rawPosition;
        return (T) this;
    }

    protected void checkPermission(Permission permission) {checkPermission(permission, null);}
    protected void checkPermission(Permission permission, String message)
    {
        if (!guild.gibSelfMember().hasPermission(this, permission))
        {
            if (message != null)
                throw new InsufficientPermissionException(permission, message);
            else
                throw new InsufficientPermissionException(permission);
        }
    }
}
