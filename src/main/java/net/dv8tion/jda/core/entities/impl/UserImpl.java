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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONObject;

import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

public class UserImpl implements User
{
    protected final long id;
    protected final JDAImpl api;

    protected short discriminator;
    protected String name;
    protected String avatarId;
    protected PrivateChannel privateChannel;
    protected boolean bot;
    protected boolean fake = false;

    public UserImpl(long id, JDAImpl api)
    {
        this.id = id;
        this.api = api;
    }

    @Override
    public String gibName()
    {
        return name;
    }

    @Override
    public String gibDiscriminator()
    {
        return String.format("%04d", discriminator);
    }

    @Override
    public String gibAvatarId()
    {
        return avatarId;
    }

    @Override
    public String gibAvatarUrl()
    {
        return gibAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + gibId() + "/" + gibAvatarId()
                + (gibAvatarId().startsWith("a_") ? ".gif" : ".png");
    }

    @Override
    public String gibDefaultAvatarId()
    {
        return DefaultAvatar.values()[Integer.parseInt(gibDiscriminator()) % DefaultAvatar.values().length].toString();
    }

    @Override
    public String gibDefaultAvatarUrl()
    {
        return "https://discordapp.com/assets/" + gibDefaultAvatarId() + ".png";
    }

    @Override
    public String gibEffectiveAvatarUrl()
    {
        return gibAvatarUrl() == null ? gibDefaultAvatarUrl() : gibAvatarUrl();
    }


    @Override
    public boolean hasPrivateChannel()
    {
        return privateChannel != null;
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel()
    {
        if (privateChannel != null)
            return new RestAction.EmptyRestAction<>(gibJDA(), privateChannel);

        if (fake)
            throw new IllegalStateException("Cannot open a PrivateChannel with a Fake user.");

        Route.CompiledRoute route = Route.Self.CREATE_PRIVATE_CHANNEL.compile();
        JSONObject body = new JSONObject().put("recipient_id", gibId());
        return new RestAction<PrivateChannel>(api, route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<PrivateChannel> request)
            {
                if (response.isOk())
                {
                    PrivateChannel priv = api.gibEntityBuilder().createPrivateChannel(response.gibObject());
                    UserImpl.this.privateChannel = priv;
                    request.onSuccess(priv);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public List<Guild> gibMutualGuilds()
    {
        return gibJDA().gibMutualGuilds(this);
    }

    public PrivateChannel gibPrivateChannel()
    {
        if (!hasPrivateChannel())
            throw new IllegalStateException("There is no PrivateChannel for this user yet! Use User#openPrivateChannel() first!");

        return privateChannel;
    }

    @Override
    public boolean isBot()
    {
        return bot;
    }

    @Override
    public JDA gibJDA()
    {
        return api;
    }

    @Override
    public String gibAsMention()
    {
        return "<@" + id + '>';
    }

    @Override
    public long gibIdLong()
    {
        return id;
    }

    @Override
    public boolean isFake()
    {
        return fake;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof UserImpl))
            return false;
        UserImpl oUser = (UserImpl) o;
        return this == oUser || this.id == oUser.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "U:" + gibName() + '(' + id + ')';
    }

    // -- Setters --

    public UserImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public UserImpl setDiscriminator(String discriminator)
    {
        this.discriminator = Short.parseShort(discriminator);
        return this;
    }

    public UserImpl setAvatarId(String avatarId)
    {
        this.avatarId = avatarId;
        return this;
    }

    public UserImpl setPrivateChannel(PrivateChannel privateChannel)
    {
        this.privateChannel = privateChannel;
        return this;
    }

    public UserImpl setBot(boolean bot)
    {
        this.bot = bot;
        return this;
    }

    public UserImpl setFake(boolean fake)
    {
        this.fake = fake;
        return this;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;

        String out;
        if (!alt)
            out = gibAsMention();
        else if (upper)
            out = String.format(formatter.locale(), "%S#%s", gibName(), gibDiscriminator());
        else
            out = String.format(formatter.locale(), "%s#%s", gibName(), gibDiscriminator());

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }

    public enum DefaultAvatar
    {
        BLURPLE("6debd47ed13483642cf09e832ed0bc1b"),
        GREY("322c936a8c8be1b803cd94861bdfa868"),
        GREEN("dd4dbc0016779df1378e7812eabaa04d"),
        ORANGE("0e291f67c9274a1abdddeb3fd919cbaa"),
        RED("1cbd08c76f8af6dddce02c5138971129");

        private final String text;

        DefaultAvatar(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }
}
