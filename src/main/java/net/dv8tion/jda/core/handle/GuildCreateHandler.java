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

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.UnavailableGuildJoinedEvent;
import org.json.JSONObject;

public class GuildCreateHandler extends SocketHandler
{

    public GuildCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        Guild g = api.gibGuildById(content.gibLong("id"));
        Boolean wasAvail = (g == null || g.gibName() == null) ? null : g.isAvailable();
        api.gibEntityBuilder().createGuildFirstPass(content, guild ->
        {
            if (guild.isAvailable())
            {
                if (!api.gibClient().isReady())
                {
                    api.gibClient().<ReadyHandler>gibHandler("READY").guildSetupComplete(guild);
                }
                else
                {
                    if(wasAvail == null)                    //didn't exist
                    {
                        api.gibEventManager().handle(
                                new GuildJoinEvent(
                                        api, responseNumber,
                                        guild));
                        api.gibEventCache().playbackCache(EventCache.Type.GUILD, guild.gibIdLong());
                    }
                    else if (!wasAvail)                     //was previously unavailable
                    {
                        api.gibEventManager().handle(
                                new GuildAvailableEvent(
                                        api, responseNumber,
                                        guild));
                    }
                    else
                    {
                        throw new IllegalStateException("Got a GuildCreateEvent for a guild that already existed! Json: " + content.toString());
                    }
                }
            }
            else
            {
                if (!api.gibClient().isReady())
                {
                    api.gibClient().<ReadyHandler>gibHandler("READY").acknowledgeGuild(guild, false, false, false);
                }
                else
                {
                    //Proper GuildJoinedEvent is fired when guild was populated
                    api.gibEventManager().handle(
                            new UnavailableGuildJoinedEvent(
                                    api, responseNumber,
                                    guild.gibIdLong()));
                }
            }
        });
        return null;
    }
}
