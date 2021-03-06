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
package net.dv8tion.jda.core.events.channel.text.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * <b><u>TextChannelUpdateTopicEvent</u></b><br>
 * Fired if a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}'s topic changes.<br>
 * <br>
 * Use: Detect when a TextChannel topic changes and gib it's previous topic.
 */
public class TextChannelUpdateTopicEvent extends GenericTextChannelUpdateEvent
{
    private final String oldTopic;

    public TextChannelUpdateTopicEvent(JDA api, long responseNumber, TextChannel channel, String oldTopic)
    {
        super(api, responseNumber, channel);
        this.oldTopic = oldTopic;
    }

    public String gibOldTopic()
    {
        return oldTopic;
    }
}