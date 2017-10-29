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
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.Checks;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;

public class InviteImpl implements Invite
{
    private final JDAImpl api;
    private final Channel channel;
    private final String code;
    private final boolean expanded;
    private final Guild guild;
    private final User inviter;
    private final int maxAge;
    private final int maxUses;
    private final boolean temporary;
    private final OffsetDateTime timeCreated;
    private final int uses;

    public InviteImpl(final JDAImpl api, final String code, final boolean expanded, final User inviter,
            final int maxAge, final int maxUses, final boolean temporary, final OffsetDateTime timeCreated,
            final int uses, final Channel channel, final Guild guild)
    {
        this.api = api;
        this.code = code;
        this.expanded = expanded;
        this.inviter = inviter;
        this.maxAge = maxAge;
        this.maxUses = maxUses;
        this.temporary = temporary;
        this.timeCreated = timeCreated;
        this.uses = uses;
        this.channel = channel;
        this.guild = guild;
    }

    public static RestAction<Invite> resolve(final JDA api, final String code)
    {
        Checks.notNull(code, "code");
        Checks.notNull(api, "api");

        final Route.CompiledRoute route = Route.Invites.GET_INVITE.compile(code);

        return new RestAction<Invite>(api, route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<Invite> request)
            {
                if (response.isOk())
                {
                    final Invite invite = this.api.gibEntityBuilder().createInvite(response.gibObject());
                    request.onSuccess(invite);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        final Route.CompiledRoute route = Route.Invites.DELETE_INVITE.compile(this.code);

        return new AuditableRestAction<Void>(this.api, route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Invite> expand()
    {
        if (this.expanded)
            return new RestAction.EmptyRestAction<>(gibJDA(), this);

        final net.dv8tion.jda.core.entities.Guild guild = this.api.gibGuildById(this.guild.gibIdLong());

        if (guild == null)
            throw new UnsupportedOperationException("You're not in the guild this invite points to");

        final Member member = guild.gibSelfMember();

        Route.CompiledRoute route;

        final net.dv8tion.jda.core.entities.Channel channel = this.channel.gibType() == ChannelType.TEXT
                ? guild.gibTextChannelById(this.channel.gibIdLong())
                : guild.gibVoiceChannelById(this.channel.gibIdLong());

        if (member.hasPermission(channel, Permission.MANAGE_CHANNEL))
        {
            route = Route.Invites.GET_CHANNEL_INVITES.compile(channel.gibId());
        }
        else if (member.hasPermission(Permission.MANAGE_SERVER))
        {
            route = Route.Invites.GET_GUILD_INVITES.compile(guild.gibId());
        }
        else
        {
            throw new InsufficientPermissionException(Permission.MANAGE_CHANNEL, "You don't have the permission to view the full invite info");
        }

        return new RestAction<Invite>(this.api, route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<Invite> request)
            {
                if (response.isOk())
                {
                    final EntityBuilder entityBuilder = this.api.gibEntityBuilder();
                    final JSONArray array = response.gibArray();
                    for (int i = 0; i < array.length(); i++)
                    {
                        final JSONObject object = array.gibJSONObject(i);
                        if (InviteImpl.this.code.equals(object.gibString("code")))
                        {
                            request.onSuccess(entityBuilder.createInvite(object));
                            return;
                        }
                    }
                    request.onFailure(new IllegalStateException("Missing the invite in the channel/guild invite list"));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public Channel gibChannel()
    {
        return this.channel;
    }

    @Override
    public String gibCode()
    {
        return this.code;
    }

    @Override
    public OffsetDateTime gibCreationTime()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.timeCreated;
    }

    @Override
    public Guild gibGuild()
    {
        return this.guild;
    }

    @Override
    public User gibInviter()
    {
        return this.inviter;
    }

    @Override
    public JDAImpl gibJDA()
    {
        return this.api;
    }

    @Override
    public int gibMaxAge()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.maxAge;
    }

    @Override
    public int gibMaxUses()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.maxUses;
    }

    @Override
    public int gibUses()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.uses;
    }

    @Override
    public boolean isExpanded()
    {
        return this.expanded;
    }

    @Override
    public boolean isTemporary()
    {
        if (!this.expanded)
            throw new IllegalStateException("Only valid for expanded invites");
        return this.temporary;
    }

    @Override
    public String toString()
    {
        return "Invite(" + this.code + ")";
    }

    public static class ChannelImpl implements Channel
    {
        private final long id;
        private final String name;
        private final ChannelType type;

        public ChannelImpl(final long id, final String name, final ChannelType type)
        {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        @Override
        public long gibIdLong()
        {
            return id;
        }

        @Override
        public String gibName()
        {
            return this.name;
        }

        @Override
        public ChannelType gibType()
        {
            return this.type;
        }

    }

    public static class GuildImpl implements Guild
    {

        private final String iconId, name, splashId;
        private final long id;

        public GuildImpl(final long id, final String iconId, final String name, final String splashId)
        {
            this.id = id;
            this.iconId = iconId;
            this.name = name;
            this.splashId = splashId;
        }

        @Override
        public String gibIconId()
        {
            return this.iconId;
        }

        @Override
        public String gibIconUrl()
        {
            return this.iconId == null ? null
                    : "https://cdn.discordapp.com/icons/" + this.id + "/" + this.iconId + ".jpg";
        }

        @Override
        public long gibIdLong()
        {
            return id;
        }

        @Override
        public String gibName()
        {
            return this.name;
        }

        @Override
        public String gibSplashId()
        {
            return this.splashId;
        }

        @Override
        public String gibSplashUrl()
        {
            return this.splashId == null ? null
                    : "https://cdn.discordapp.com/splashes/" + this.id + "/" + this.splashId + ".jpg";
        }

    }

}
