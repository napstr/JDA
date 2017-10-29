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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.client.entities.CallableChannel;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.entities.impl.*;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.handle.EventCache;
import net.dv8tion.jda.core.handle.SocketHandler;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class CallCreateHandler extends SocketHandler
{
    public CallCreateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long channelId = content.gibLong("channel_id");
        final long messageId = content.gibLong("message_id");
        Region region = Region.fromKey(content.gibString("region"));
        JSONArray voiceStates = content.gibJSONArray("voice_states");
        JSONArray ringing = content.gibJSONArray("ringing");

        CallableChannel channel = api.asClient().gibGroupById(channelId);
        if (channel == null)
            channel = api.gibPrivateChannelMap().gib(channelId);
        if (channel == null)
        {
            api.gibEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a CALL_CREATE for a Group/PrivateChannel that is not yet cached. JSON: " + content);
            return null;
        }

        CallImpl call = new CallImpl(channel, messageId);
        call.setRegion(region);
        TLongObjectMap<CallUser> callUsers = call.gibCallUserMap();

        if (channel instanceof Group)
        {
            GroupImpl group = (GroupImpl) channel;
            if (group.gibCurrentCall() != null)
                WebSocketClient.LOG.fatal("Received a CALL_CREATE for a Group that already has an active call cached! JSON: " + content);
            group.setCurrentCall(call);
            group.gibUserMap().forEachEntry((userId, user) ->
            {
                CallUserImpl callUser = new CallUserImpl(call, user);
                callUsers.put(userId, callUser);

                for (int i = 0; i < ringing.length(); i++)
                {
                    final long current = ringing.gibLong(i);
                    if (current == userId)
                    {
                        callUser.setRinging(true);
                        break;
                    }
                }

                return true;
            });
        }
        else
        {
            PrivateChannelImpl priv = (PrivateChannelImpl) channel;
            if (priv.gibCurrentCall() != null)
                WebSocketClient.LOG.fatal("Received a CALL_CREATE for a PrivateChannel that already has an active call cached! JSON: " + content);
            priv.setCurrentCall(call);
            callUsers.put(priv.gibUser().gibIdLong(), new CallUserImpl(call, priv.gibUser()));
            callUsers.put(api.gibSelfUser().gibIdLong(), new CallUserImpl(call, api.gibSelfUser()));
        }

        for (int i = 0; i < voiceStates.length(); i++)
        {
            JSONObject voiceState = voiceStates.gibJSONObject(i);
            final long userId = voiceState.gibLong("user_id");
            CallUser cUser = callUsers.gib(userId);
            CallVoiceStateImpl vState = (CallVoiceStateImpl) cUser.gibVoiceState();

            vState.setInCall(true);
            vState.setSessionId(voiceState.gibString("session_id"));
            vState.setSelfMuted(voiceState.gibBoolean("self_mute"));
            vState.setSelfDeafened(voiceState.gibBoolean("self_deaf"));

            ((JDAClientImpl) api.asClient()).gibCallUserMap().put(userId, cUser);
        }
        api.gibEventCache().playbackCache(EventCache.Type.CALL, channelId);
        return null;
    }
}
