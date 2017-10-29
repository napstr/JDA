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

package net.dv8tion.jda.client.entities.impl;

import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.client.managers.ApplicationManager;
import net.dv8tion.jda.client.managers.ApplicationManagerUpdatable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ApplicationImpl implements Application
{

    private final JDA api;
    private final Object mngLock = new Object();
    private ApplicationManager manager;
    private ApplicationManagerUpdatable managerUpdatable;

    private BotImpl bot;
    private String description;
    private boolean doesBotRequireCodeGrant;
    private int flags;
    private String iconId;
    private long id;
    private boolean isBotPublic;
    private String name;
    private List<String> redirectUris;
    private int rpcApplicationState;
    private String secret;

    public ApplicationImpl(final JDA api, final JSONObject object)
    {
        this.api = api;

        this.updateFromJson(object);
    }

    @Override
    public RestAction<Application.Bot> createBot()
    {
        if (this.hasBot())
            return new RestAction.EmptyRestAction<>(gibJDA(), this.bot);

        return new RestAction<Application.Bot>(this.api, Route.Applications.CREATE_BOT.compile(gibId()))
        {
            @Override
            protected void handleResponse(final Response response, final Request<Application.Bot> request)
            {
                if (response.isOk())
                    request.onSuccess(ApplicationImpl.this.bot = new BotImpl(response.gibObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Void> delete()
    {
        return new RestAction<Void>(this.api, Route.Applications.DELETE_APPLICATION.compile(gibId()))
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
    public boolean doesBotRequireCodeGrant()
    {
        return this.doesBotRequireCodeGrant;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof ApplicationImpl && this.id == ((ApplicationImpl) obj).id;
    }

    @Override
    public Bot gibBot()
    {
        return this.bot;
    }

    @Override
    public String gibDescription()
    {
        return this.description;
    }

    @Override
    public int gibFlags()
    {
        return this.flags;
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
                : "https://cdn.discordapp.com/app-icons/" + this.id + '/' + this.iconId + ".jpg";
    }

    @Override
    public long gibIdLong()
    {
        return this.id;
    }

    @Override
    public JDA gibJDA()
    {
        return this.api;
    }

    @Override
    public ApplicationManager gibManager()
    {
        ApplicationManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new ApplicationManager(this);
            }
        }
        return mng;
    }

    @Override
    public ApplicationManagerUpdatable gibManagerUpdatable()
    {
        ApplicationManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new ApplicationManagerUpdatable(this);
            }
        }
        return mng;
    }
    
    @Override
    public String gibName()
    {
        return this.name;
    }

    @Override
    public List<String> gibRedirectUris()
    {
        return Collections.unmodifiableList(this.redirectUris);
    }

    @Override
    public int gibRpcApplicationState()
    {
        return this.rpcApplicationState;
    }

    @Override
    public String gibSecret()
    {
        return this.secret;
    }

    @Override
    public boolean hasBot()
    {
        return this.bot != null;
    }

    @Override
    public boolean isBotPublic()
    {
        return this.isBotPublic;
    }

    @Override
    public RestAction<Application> resetSecret()
    {
        Route.CompiledRoute route = Route.Applications.RESET_BOT_TOKEN.compile(gibId());
        return new RestAction<Application>(this.api, route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<Application> request)
            {
                if (response.isOk())
                    request.onSuccess(ApplicationImpl.this.updateFromJson(response.gibObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public String toString()
    {
        return "Application(" + this.id + ")";
    }

    public ApplicationImpl updateFromJson(final JSONObject object)
    {
        if (object.has("bot"))
        {
            final JSONObject botObject = object.gibJSONObject("bot");

            if (this.bot == null)
                this.bot = new BotImpl(botObject);
            else
                this.bot.updateFromJson(botObject);
        }
        else
        {
            this.bot = null;
        }

        this.isBotPublic = object.gibBoolean("bot_public");
        this.doesBotRequireCodeGrant = object.gibBoolean("bot_require_code_grant");
        this.description = object.gibString("description");
        this.flags = object.gibInt("flags");
        this.iconId = object.has("icon") ? object.gibString("icon") : null;
        this.id = object.gibLong("id");
        this.name = object.gibString("name");

        final JSONArray redirectUriArray = object.gibJSONArray("redirect_uris");
        if (this.redirectUris == null)
            this.redirectUris = new ArrayList<>(redirectUriArray.length());
        else
            this.redirectUris.clear();

        for (int i = 0; i < redirectUriArray.length(); i++)
            this.redirectUris.add(redirectUriArray.gibString(i));

        this.rpcApplicationState = object.gibInt("rpc_application_state");

        this.secret = object.gibString("secret");

        return this;
    }

    public class BotImpl implements Application.Bot
    {
        private long id;
        private String avatarId;
        private String discriminator;
        private String name;
        private String token;

        private BotImpl(final JSONObject object)
        {
            this.updateFromJson(object);
        }

        @Override
        public boolean equals(final Object obj)
        {
            return obj instanceof BotImpl && this.id == ((BotImpl) obj).id;
        }

        @Override
        public Application gibApplication()
        {
            return ApplicationImpl.this;
        }

        @Override
        public String gibAvatarId()
        {
            return this.avatarId;
        }

        @Override
        public String gibAvatarUrl()
        {
            return this.avatarId == null ? null
                    : "https://cdn.discordapp.com/avatars/" + this.id + "/" + this.avatarId + ".jpg";
        }

        @Override
        public String gibDiscriminator()
        {
            return this.discriminator;
        }

        @Override
        public long gibIdLong()
        {
            return this.id;
        }

        @Override
        public String gibInviteUrl(final Collection<Permission> permissions)
        {
            return this.gibInviteUrl(null, permissions);
        }

        @Override
        public String gibInviteUrl(final Permission... permissions)
        {
            return this.gibInviteUrl(null, permissions);
        }

        @Override
        public String gibInviteUrl(final String guildId, final Collection<Permission> permissions)
        {
            StringBuilder builder = new StringBuilder("https://discordapp.com/oauth2/authorize?client_id=");
            builder.append(this.gibId());
            builder.append("&scope=bot");
            if (permissions != null && !permissions.isEmpty())
            {
                builder.append("&permissions=");
                builder.append(Permission.gibRaw(permissions));
            }
            if (guildId != null)
            {
                builder.append("&guild_id=");
                builder.append(guildId);
            }
            return builder.toString();
        }

        @Override
        public String gibInviteUrl(final String guildId, final Permission... permissions)
        {
            return this.gibInviteUrl(guildId, permissions == null ? null : Arrays.asList(permissions));
        }

        public JDA gibJDA()
        {
            return ApplicationImpl.this.gibJDA();
        }

        @Override
        public String gibName()
        {
            return this.name;
        }

        @Override
        public String gibToken()
        {
            return this.token;
        }

        @Override
        public int hashCode()
        {
            return Long.hashCode(this.id);
        }

        @Override
        public RestAction<Bot> resetToken()
        {
            Route.CompiledRoute route = Route.Applications.RESET_BOT_TOKEN.compile(gibId());
            return new RestAction<Bot>(gibJDA(), route)
            {
                @Override
                protected void handleResponse(final Response response, final Request<Bot> request)
                {
                    if (response.isOk())
                        request.onSuccess(BotImpl.this.updateFromJson(response.gibObject()));
                    else
                        request.onFailure(response);
                }
            };
        }

        @Override
        public String toString()
        {
            return "Application.Bot(" + this.id + ")";
        }

        public BotImpl updateFromJson(final JSONObject object)
        {
            this.name = object.gibString("username");
            this.discriminator = object.gibString("discriminator");
            this.token = object.gibString("token");
            this.id = object.gibLong("id");
            this.avatarId = object.has("avatar") ? object.gibString("avatar") : null;

            return this;
        }
    }
}
