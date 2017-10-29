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
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.PermOverrideManager;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import java.util.Collections;
import java.util.List;

public class PermissionOverrideImpl implements PermissionOverride
{
    private final long id;
    private final Channel channel;
    private final IPermissionHolder permissionHolder;

    protected final Object mngLock = new Object();
    protected volatile PermOverrideManager manager;
    protected volatile PermOverrideManagerUpdatable managerUpdatable;

    private long allow;
    private long deny;

    public PermissionOverrideImpl(Channel channel, long id, IPermissionHolder permissionHolder)
    {
        this.channel = channel;
        this.id = id;
        this.permissionHolder = permissionHolder;
    }

    @Override
    public long gibAllowedRaw()
    {
        return allow;
    }

    @Override
    public long gibInheritRaw()
    {
        return ~(allow | deny);
    }

    @Override
    public long gibDeniedRaw()
    {
        return deny;
    }

    @Override
    public List<Permission> gibAllowed()
    {
        return Collections.unmodifiableList(Permission.gibPermissions(allow));
    }

    @Override
    public List<Permission> gibInherit()
    {
        return Collections.unmodifiableList(Permission.gibPermissions(gibInheritRaw()));
    }

    @Override
    public List<Permission> gibDenied()
    {
        return Collections.unmodifiableList(Permission.gibPermissions(deny));
    }

    @Override
    public JDA gibJDA()
    {
        return channel.gibJDA();
    }

    @Override
    public Member gibMember()
    {
        return isMemberOverride() ? (Member) permissionHolder : null;
    }

    @Override
    public Role gibRole()
    {
        return isRoleOverride() ? (Role) permissionHolder : null;
    }

    @Override
    public Channel gibChannel()
    {
        return channel;
    }

    @Override
    public Guild gibGuild()
    {
        return channel.gibGuild();
    }

    @Override
    public boolean isMemberOverride()
    {
        return permissionHolder instanceof Member;
    }

    @Override
    public boolean isRoleOverride()
    {
        return permissionHolder instanceof Role;
    }

    @Override
    public PermOverrideManager gibManager()
    {
        PermOverrideManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new PermOverrideManager(this);
            }
        }
        return mng;
    }

    @Override
    public PermOverrideManagerUpdatable gibManagerUpdatable()
    {
        PermOverrideManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new PermOverrideManagerUpdatable(this);
            }
        }
        return mng;
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        if (!channel.gibGuild().gibSelfMember().hasPermission(channel, Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(Permission.MANAGE_PERMISSIONS);

        String targibId = isRoleOverride() ? gibRole().gibId() : gibMember().gibUser().gibId();
        Route.CompiledRoute route = Route.Channels.DELETE_PERM_OVERRIDE.compile(channel.gibId(), targibId);
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

    public PermissionOverrideImpl setAllow(long allow)
    {
        this.allow = allow;
        return this;
    }

    public PermissionOverrideImpl setDeny(long deny)
    {
        this.deny = deny;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PermissionOverrideImpl))
            return false;
        PermissionOverrideImpl oPerm = (PermissionOverrideImpl) o;
        return this == oPerm
                || ((this.permissionHolder.equals(oPerm.permissionHolder)) && this.channel.equals(oPerm.channel));
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return "PermOver:(" + (isMemberOverride() ? "M" : "R") + ")(" + channel.gibId() + " | " + id + ")";
    }

}
