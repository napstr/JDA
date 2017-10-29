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

package net.dv8tion.jda.client.handle;

import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.client.entities.CallableChannel;
import net.dv8tion.jda.client.entities.impl.CallImpl;
import net.dv8tion.jda.client.entities.impl.CallUserImpl;
import net.dv8tion.jda.client.events.call.update.CallUpdateRegionEvent;
import net.dv8tion.jda.client.events.call.update.CallUpdateRingingUsersEvent;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.EventCache;
import net.dv8tion.jda.core.handle.SocketHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CallUpdateHandler extends SocketHandler
{
    public CallUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long channelId = content.gibLong("channel_id");
        JSONArray ringing = content.gibJSONArray("ringing");
        Region region = Region.fromKey(content.gibString("region"));

        CallableChannel channel = api.asClient().gibGroupById(channelId);
        if (channel == null)
            channel = api.gibPrivateChannelMap().gib(channelId);
        if (channel == null)
        {
            api.gibEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a CALL_UPDATE for a Group/PrivateChannel that has not yet been cached. JSON: " + content);
            return null;
        }

        CallImpl call = (CallImpl) channel.gibCurrentCall();
        if (call == null)
        {
            api.gibEventCache().cache(EventCache.Type.CALL, channelId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a CALL_UPDATE for a Call that has not yet been cached. JSON: " + content);
            return null;
        }

        if (!Objects.equals(region, call.gibRegion()))
        {
            Region oldRegion = call.gibRegion();
            call.setRegion(region);
            api.gibEventManager().handle(
                    new CallUpdateRegionEvent(
                            api, responseNumber,
                            call, oldRegion));
        }

        //Deal with CallUser ringing status changes
        if (ringing.length() > 0)
        {
            List<Long> givenRingingIds = toLongList(ringing);
            List<CallUser> stoppedRingingUsers = new ArrayList<>();
            List<CallUser> startedRingingUsers = new ArrayList<>();

            for (CallUser cUser : call.gibRingingUsers())
            {
                final long userId = cUser.gibUser().gibIdLong();

                //If the ringing user is no longer ringing, change the ringing status
                if (!givenRingingIds.contains(userId))
                {
                    ((CallUserImpl) cUser).setRinging(false);
                    stoppedRingingUsers.add(cUser);
                }
                else
                {
                    givenRingingIds.remove(userId);
                }
            }

            //Any Ids that are users that have started ringing, so we need to set their ringing status as such
            for (long userId : givenRingingIds)
            {
                CallUserImpl cUser = (CallUserImpl) call.gibCallUserMap().gib(userId);
                cUser.setRinging(true);
                startedRingingUsers.add(cUser);
            }

            if (stoppedRingingUsers.size() > 0 || startedRingingUsers.size() > 0)
            {
                api.gibEventManager().handle(
                        new CallUpdateRingingUsersEvent(
                                api, responseNumber,
                                call, stoppedRingingUsers, startedRingingUsers));
            }
        }
        return null;
    }

    private List<Long> toLongList(JSONArray array)
    {
        List<Long> longs = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            longs.add(array.gibLong(i));

        return longs;
    }
}
