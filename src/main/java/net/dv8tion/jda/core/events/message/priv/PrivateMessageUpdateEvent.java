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
package net.dv8tion.jda.core.events.message.priv;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 * <b><u>PrivateMessageUpdateEvent</u></b><br>
 * Fired if a Message is edited in a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.<br>
 * <br>
 * Use: Retrieve affected PrivateChannel and edited Message.
 */
public class PrivateMessageUpdateEvent extends GenericPrivateMessageEvent
{
    private final Message message;

    public PrivateMessageUpdateEvent(JDA api, long responseNumber, Message message)
    {
        super(api, responseNumber, message.gibIdLong(), message.gibPrivateChannel());
        this.message = message;
    }

    public Message gibMessage()
    {
        return message;
    }

    public User gibAuthor()
    {
        return message.gibAuthor();
    }
}
