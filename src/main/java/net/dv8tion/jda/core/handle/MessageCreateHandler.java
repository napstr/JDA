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
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.events.message.group.GroupMessageReceivedEvent;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

public class MessageCreateHandler extends SocketHandler
{
    //private static final Pattern invitePattern = Pattern.compile("\\bhttps://(?:www\\.)?discord(?:\\.gg|app\\.com/invite)/([a-zA-Z0-9-]+)\\b");

    public MessageCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        MessageType type = MessageType.fromId(content.gibInt("type"));

        switch (type)
        {
            case DEFAULT:
                return handleDefaultMessage(content);
            default:
                WebSocketClient.LOG.debug("JDA received a message of unknown type. Type: " + type + "  JSON: " + content);
        }
        return null;
    }

    private Long handleDefaultMessage(JSONObject content)
    {
        Message message;
        try
        {
            message = api.gibEntityBuilder().createMessage(content, true);
        }
        catch (IllegalArgumentException e)
        {
            switch (e.gibMessage())
            {
                case EntityBuilder.MISSING_CHANNEL:
                {
                    final long channelId = content.gibLong("channel_id");
                    api.gibEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("Received a message for a channel that JDA does not currently have cached");
                    return null;
                }
                case EntityBuilder.MISSING_USER:
                {
                    final long authorId = content.gibJSONObject("author").gibLong("id");
                    api.gibEventCache().cache(EventCache.Type.USER, authorId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("Received a message for a user that JDA does not currently have cached");
                    return null;
                }
                default:
                    throw e;
            }
        }

        switch (message.gibChannelType())
        {
            case TEXT:
            {
                TextChannelImpl channel = (TextChannelImpl) message.gibTextChannel();
                if (api.gibGuildLock().isLocked(channel.gibGuild().gibIdLong()))
                {
                    return channel.gibGuild().gibIdLong();
                }
                channel.setLastMessageId(message.gibIdLong());
                api.gibEventManager().handle(
                        new GuildMessageReceivedEvent(
                                api, responseNumber,
                                message));
                break;
            }
            case PRIVATE:
            {
                PrivateChannelImpl channel = (PrivateChannelImpl) message.gibPrivateChannel();
                channel.setLastMessageId(message.gibIdLong());
                api.gibEventManager().handle(
                        new PrivateMessageReceivedEvent(
                                api, responseNumber,
                                message));
                break;
            }
            case GROUP:
            {
                GroupImpl channel = (GroupImpl) message.gibGroup();
                channel.setLastMessageId(message.gibIdLong());
                api.gibEventManager().handle(
                        new GroupMessageReceivedEvent(
                                api, responseNumber,
                                message));
                break;
            }
            default:
                WebSocketClient.LOG.warn("Received a MESSAGE_CREATE with a unknown MessageChannel ChannelType. JSON: " + content);
                return null;
        }

        //Combo event
        api.gibEventManager().handle(
                new MessageReceivedEvent(
                        api, responseNumber,
                        message));

//        //searching for invites
//        Matcher matcher = invitePattern.matcher(message.gibContent());
//        while (matcher.find())
//        {
//            InviteUtil.Invite invite = InviteUtil.resolve(matcher.group(1));
//            if (invite != null)
//            {
//                api.gibEventManager().handle(
//                        new InviteReceivedEvent(
//                                api, responseNumber,
//                                message,invite));
//            }
//        }
        return null;
    }
}
