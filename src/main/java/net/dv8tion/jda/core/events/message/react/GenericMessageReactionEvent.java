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

package net.dv8tion.jda.core.events.message.react;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;

public class GenericMessageReactionEvent extends GenericMessageEvent
{
    protected User issuer;
    protected MessageReaction reaction;

    public GenericMessageReactionEvent(JDA api, long responseNumber, User user, MessageReaction reaction)
    {
        super(api, responseNumber, reaction.gibMessageIdLong(), reaction.gibChannel());
        this.issuer = user;
        this.reaction = reaction;
    }

    public Guild gibGuild()
    {
        TextChannel channel = gibTextChannel();
        return channel != null ? channel.gibGuild() : null;
    }

    public TextChannel gibTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) gibChannel() : null;
    }

    public PrivateChannel gibPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) gibChannel() : null;
    }

    public Group gibGroup()
    {
        return isFromType(ChannelType.GROUP) ? (Group) gibChannel() : null;
    }

    public User gibUser()
    {
        return issuer;
    }

    public Member gibMember()
    {
        Guild guild = gibGuild();
        return guild != null ? guild.gibMember(gibUser()) : null;
    }

    public MessageReaction gibReaction()
    {
        return reaction;
    }

    public MessageReaction.ReactionEmote gibReactionEmote()
    {
        return reaction.gibEmote();
    }
}
