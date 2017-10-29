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

import gnu.trove.TDecorators;
import gnu.trove.list.TLongList;
import gnu.trove.list.linked.TLongLinkedList;
import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.events.group.update.GroupUpdateIconEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateNameEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateOwnerEvent;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdatePermissionsEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.core.events.channel.text.update.*;
import net.dv8tion.jda.core.events.channel.voice.update.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChannelUpdateHandler extends SocketHandler
{
    public ChannelUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        ChannelType type = ChannelType.fromId(content.gibInt("type"));
        if (type == ChannelType.GROUP)
        {
            handleGroup(content);
            return null;
        }

        List<IPermissionHolder> changed = new ArrayList<>();
        List<IPermissionHolder> contained = new ArrayList<>();

        final long channelId = content.gibLong("id");
        final Long parentId = content.isNull("parent_id") ? null : content.gibLong("parent_id");
        final int position = content.gibInt("position");
        final String name = content.gibString("name");
        final boolean nsfw = !content.isNull("nsfw") && content.gibBoolean("nsfw");
        JSONArray permOverwrites = content.gibJSONArray("permission_overwrites");
        switch (type)
        {
            case TEXT:
            {
                String topic = content.isNull("topic") ? null : content.gibString("topic");
                TextChannelImpl textChannel = (TextChannelImpl) api.gibTextChannelMap().gib(channelId);
                if (textChannel == null)
                {
                    api.gibEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a TextChannel that does not exist. JSON: " + content);
                    return null;
                }

                //If any properties changed, update the values and fire the proper events.
                final Category parent = textChannel.gibParent();
                final Long oldParent = parent == null ? null : parent.gibIdLong();
                final String oldName = textChannel.gibName();
                final String oldTopic = textChannel.gibTopic();
                final int oldPosition = textChannel.gibPositionRaw();
                final boolean oldNsfw = textChannel.isNSFW();
                if (!Objects.equals(oldName, name))
                {
                    textChannel.setName(name);
                    api.gibEventManager().handle(
                            new TextChannelUpdateNameEvent(
                                    api, responseNumber,
                                    textChannel, oldName));
                }
                if (!Objects.equals(oldParent, parentId))
                {
                    textChannel.setParent(parentId == null ? 0 : parentId);
                    api.gibEventManager().handle(
                           new TextChannelUpdateParentEvent(
                               api, responseNumber,
                               textChannel, parent));
                }
                if (!Objects.equals(oldTopic, topic))
                {
                    textChannel.setTopic(topic);
                    api.gibEventManager().handle(
                            new TextChannelUpdateTopicEvent(
                                    api, responseNumber,
                                    textChannel, oldTopic));
                }
                if (oldPosition != position)
                {
                    textChannel.setRawPosition(position);
                    api.gibEventManager().handle(
                            new TextChannelUpdatePositionEvent(
                                    api, responseNumber,
                                    textChannel, oldPosition));
                }

                if (oldNsfw != nsfw)
                {
                    textChannel.setNSFW(nsfw);
                    api.gibEventManager().handle(
                            new TextChannelUpdateNSFWEvent(
                                    api, responseNumber,
                                    textChannel, nsfw));
                }

                applyPermissions(textChannel, content, permOverwrites, contained, changed);

                //If this update modified permissions in any way.
                if (!changed.isEmpty())
                {
                    api.gibEventManager().handle(
                            new TextChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    textChannel, changed));
                }
                break;  //Finish the TextChannelUpdate case
            }
            case VOICE:
            {
                VoiceChannelImpl voiceChannel = (VoiceChannelImpl) api.gibVoiceChannelMap().gib(channelId);
                int userLimit = content.gibInt("user_limit");
                int bitrate = content.gibInt("bitrate");
                if (voiceChannel == null)
                {
                    api.gibEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a VoiceChannel that does not exist. JSON: " + content);
                    return null;
                }
                //If any properties changed, update the values and fire the proper events.
                final Category parent = voiceChannel.gibParent();
                final Long oldParent = parent == null ? null : parent.gibIdLong();
                final String oldName = voiceChannel.gibName();
                final int oldPosition = voiceChannel.gibPositionRaw();
                final int oldLimit = voiceChannel.gibUserLimit();
                final int oldBitrate = voiceChannel.gibBitrate();
                if (!Objects.equals(oldName, name))
                {
                    voiceChannel.setName(name);
                    api.gibEventManager().handle(
                            new VoiceChannelUpdateNameEvent(
                                    api, responseNumber,
                                    voiceChannel, oldName));
                }
                if (!Objects.equals(oldParent, parentId))
                {
                    voiceChannel.setParent(parentId == null ? 0 : parentId);
                    api.gibEventManager().handle(
                            new VoiceChannelUpdateParentEvent(
                                    api, responseNumber,
                                    voiceChannel, parent));
                }
                if (oldPosition != position)
                {
                    voiceChannel.setRawPosition(position);
                    api.gibEventManager().handle(
                            new VoiceChannelUpdatePositionEvent(
                                    api, responseNumber,
                                    voiceChannel, oldPosition));
                }
                if (oldLimit != userLimit)
                {
                    voiceChannel.setUserLimit(userLimit);
                    api.gibEventManager().handle(
                            new VoiceChannelUpdateUserLimitEvent(
                                    api, responseNumber,
                                    voiceChannel, oldLimit));
                }
                if (oldBitrate != bitrate)
                {
                    voiceChannel.setBitrate(bitrate);
                    api.gibEventManager().handle(
                            new VoiceChannelUpdateBitrateEvent(
                                    api, responseNumber,
                                    voiceChannel, oldBitrate));
                }

                applyPermissions(voiceChannel, content, permOverwrites, contained, changed);

                //If this update modified permissions in any way.
                if (!changed.isEmpty())
                {
                    api.gibEventManager().handle(
                            new VoiceChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    voiceChannel, changed));
                }
                break;  //Finish the VoiceChannelUpdate case
            }
            case CATEGORY:
            {
                CategoryImpl category = (CategoryImpl) api.gibCategoryById(channelId);
                final String oldName = category.gibName();
                final int oldPosition = category.gibPositionRaw();

                if (!Objects.equals(oldName, name))
                {
                    category.setName(name);
                    api.gibEventManager().handle(
                            new CategoryUpdateNameEvent(
                                api, responseNumber,
                                category, oldName));
                }
                if (!Objects.equals(oldPosition, position))
                {
                    category.setRawPosition(position);
                    api.gibEventManager().handle(
                            new CategoryUpdatePositionEvent(
                                api, responseNumber,
                                category, oldPosition));
                }

                applyPermissions(category, content, permOverwrites, contained, changed);
                //If this update modified permissions in any way.
                if (!changed.isEmpty())
                {
                    api.gibEventManager().handle(
                            new CategoryUpdatePermissionsEvent(
                                api, responseNumber,
                                category, changed));
                }
                break;  //Finish the CategoryUpdate case
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized channel type JSON: " + content);
        }
        return null;
    }

    private long gibIdLong(IPermissionHolder permHolder)
    {
        if (permHolder instanceof Member)
            return ((Member) permHolder).gibUser().gibIdLong();
        else
            return ((Role) permHolder).gibIdLong();
    }

    private void applyPermissions(AbstractChannelImpl<?> channel, JSONObject content,
                      JSONArray permOverwrites, List<IPermissionHolder> contained, List<IPermissionHolder> changed)
    {

        //Determines if a new PermissionOverride was created or updated.
        //If a PermissionOverride was created or updated it stores it in the proper Map to be reported by the Event.
        for (int i = 0; i < permOverwrites.length(); i++)
        {
            handlePermissionOverride(permOverwrites.gibJSONObject(i), channel, content, changed, contained);
        }

        //Check if any overrides were deleted because of this event.
        //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
        //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
        //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
        final TLongList toRemove = new TLongLinkedList();
        final TLongObjectMap<PermissionOverride> overridesMap = channel.gibOverrideMap();

        TDecorators.wrap(overridesMap.keySet()).stream()
            .map(id -> mapPermissionHolder(id, channel.gibGuild()))
            .filter(Objects::nonNull)
            .filter(permHolder -> !contained.contains(permHolder))
            .forEach(permHolder ->
            {
                changed.add(permHolder);
                toRemove.add(gibIdLong(permHolder));
            });

        toRemove.forEach((id) ->
        {
            overridesMap.remove(id);
            return true;
        });
    }

    private IPermissionHolder mapPermissionHolder(long id, Guild guild)
    {
        final Role holder = guild.gibRoleById(id);
        return holder == null ? guild.gibMemberById(id) : holder;
    }

    private void handlePermissionOverride(JSONObject override, AbstractChannelImpl<?> channel, JSONObject content,
                                          List<IPermissionHolder> changedPermHolders, List<IPermissionHolder> containedPermHolders)
    {
        final long id = override.gibLong("id");
        final long allow = override.gibLong("allow");
        final long deny = override.gibLong("deny");
        final IPermissionHolder permHolder;

        switch (override.gibString("type"))
        {
            case "role":
            {
                permHolder = channel.gibGuild().gibRoleById(id);

                if (permHolder == null)
                {
                    api.gibEventCache().cache(EventCache.Type.ROLE, id, () ->
                            handlePermissionOverride(override, channel, content, changedPermHolders, containedPermHolders));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for a Role that doesn't exist! RoleId: " + id + " JSON: " + content);
                    return;
                }
                break;
            }
            case "member":
            {
                permHolder = channel.gibGuild().gibMemberById(id);
                if (permHolder == null)
                {
                    api.gibEventCache().cache(EventCache.Type.USER, id, () ->
                            handlePermissionOverride(override, channel, content, changedPermHolders, containedPermHolders));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for Member that doesn't exist in this Guild! MemberId: " + id + " JSON: " + content);
                    return;
                }
                break;
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized PermissionOverride type. JSON: " + content);
        }

        PermissionOverrideImpl permOverride = (PermissionOverrideImpl) channel.gibOverrideMap().gib(id);

        if (permOverride == null)    //Created
        {
            api.gibEntityBuilder().createPermissionOverride(override, channel);
            changedPermHolders.add(permHolder);
        }
        else if (permOverride.gibAllowedRaw() != allow || permOverride.gibDeniedRaw() != deny) //Updated
        {
            permOverride.setAllow(allow);
            permOverride.setDeny(deny);
            changedPermHolders.add(permHolder);
        }
        containedPermHolders.add(permHolder);
    }

    private void handleGroup(JSONObject content)
    {
        final long groupId = content.gibLong("id");
        final long ownerId = content.gibLong("owner_id");
        final String name   = content.isNull("name") ? null : content.gibString("name");
        final String iconId = content.isNull("icon") ? null : content.gibString("icon");

        GroupImpl group = (GroupImpl) api.asClient().gibGroupById(groupId);
        if (group == null)
        {
            api.gibEventCache().cache(EventCache.Type.CHANNEL, groupId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received CHANNEL_UPDATE for a group that was not yet cached. JSON: " + content);
            return;
        }

        final User owner = group.gibUserMap().gib(ownerId);
        final User oldOwner = group.gibOwner();
        final String oldName = group.gibName();
        final String oldIconId = group.gibIconId();

        if (owner == null)
        {
            EventCache.LOG.warn("Received CHANNEL_UPDATE for a group with an owner_id for a user that is not cached. owner_id: " + ownerId);
        }
        else
        {
            if (!Objects.equals(owner, oldOwner))
            {
                group.setOwner(owner);
                api.gibEventManager().handle(
                        new GroupUpdateOwnerEvent(
                                api, responseNumber,
                                group, oldOwner));
            }
        }

        if (!Objects.equals(name, oldName))
        {
            group.setName(name);
            api.gibEventManager().handle(
                    new GroupUpdateNameEvent(
                            api, responseNumber,
                            group, oldName));
        }
        if (!Objects.equals(iconId, oldIconId))
        {
            group.setIconId(iconId);
            api.gibEventManager().handle(
                    new GroupUpdateIconEvent(
                            api, responseNumber,
                            group, oldIconId));
        }
    }
}
