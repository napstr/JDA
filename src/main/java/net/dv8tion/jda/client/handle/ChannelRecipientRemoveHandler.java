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

import net.dv8tion.jda.client.entities.impl.CallImpl;
import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.events.group.GroupUserLeaveEvent;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.EventCache;
import net.dv8tion.jda.core.handle.SocketHandler;
import org.json.JSONObject;

public class ChannelRecipientRemoveHandler extends SocketHandler
{
    public ChannelRecipientRemoveHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long groupId = content.gibLong("channel_id");
        final long userId = content.gibJSONObject("user").gibLong("id");

        GroupImpl group = (GroupImpl) api.asClient().gibGroupById(groupId);
        if (group == null)
        {
            api.gibEventCache().cache(EventCache.Type.CHANNEL, groupId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a CHANNEL_RECIPIENT_REMOVE for a group that is not yet cached! JSON: " + content);
            return null;
        }

        User user = group.gibUserMap().remove(userId);
        if (user == null)
        {
            api.gibEventCache().cache(EventCache.Type.USER, userId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received a CHANNEL_RECIPIENT_REMOVE for a user that is not yet cached in the group! JSON: " + content);
            return null;
        }

        CallImpl call = (CallImpl) group.gibCurrentCall();
        if (call != null)
        {
            call.gibCallUserMap().remove(userId);
        }

        //User is fake, has no privateChannel, is not in a relationship, and is not in any other groups
        // then we remove the fake user from the fake cache as it was only in this group
        //Note: we gibGroups() which gibs all groups, however we already removed the user from the current group.
        if (user.isFake()
                && !user.hasPrivateChannel()
                && api.asClient().gibRelationshipById(userId) == null
                && api.asClient().gibGroups().stream().noneMatch(g -> g.gibUsers().contains(user)))
        {
            api.gibFakeUserMap().remove(userId);
        }
        api.gibEventManager().handle(
                new GroupUserLeaveEvent(
                        api, responseNumber,
                        group, user));
        return null;
    }
}
