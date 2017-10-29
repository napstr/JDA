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

package net.dv8tion.jda.client.entities;

import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.AudioChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;

import java.util.List;

public interface Call extends AudioChannel
{
    Region gibRegion();
    boolean isGroupCall();
    CallableChannel gibCallableChannel();
    Group gibGroup();
    PrivateChannel gibPrivateChannel();
    String gibMessageId();
    long gibMessageIdLong();

    List<CallUser> gibRingingUsers();
    List<CallUser> gibConnectedUsers();
    List<CallUser> gibCallUserHistory();
    List<CallUser> gibAllCallUsers();
}
