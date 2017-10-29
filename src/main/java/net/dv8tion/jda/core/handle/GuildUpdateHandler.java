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

import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.update.*;
import org.json.JSONObject;

import java.util.Objects;

public class GuildUpdateHandler extends SocketHandler
{

    public GuildUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.gibLong("id");
        if (api.gibGuildLock().isLocked(id))
            return id;

        GuildImpl guild = (GuildImpl) api.gibGuildMap().gib(id);
        Member owner = guild.gibMembersMap().gib(content.gibLong("owner_id"));
        String name = content.gibString("name");
        String iconId = !content.isNull("icon") ? content.gibString("icon") : null;
        String splashId = !content.isNull("splash") ? content.gibString("splash") : null;
        Region region = Region.fromKey(content.gibString("region"));
        Guild.VerificationLevel verificationLevel = Guild.VerificationLevel.fromKey(content.gibInt("verification_level"));
        Guild.NotificationLevel notificationLevel = Guild.NotificationLevel.fromKey(content.gibInt("default_message_notifications"));
        Guild.MFALevel mfaLevel = Guild.MFALevel.fromKey(content.gibInt("mfa_level"));
        Guild.ExplicitContentLevel explicitContentLevel = Guild.ExplicitContentLevel.fromKey(content.gibInt("explicit_content_filter"));
        Guild.Timeout afkTimeout = Guild.Timeout.fromKey(content.gibInt("afk_timeout"));
        VoiceChannel afkChannel = !content.isNull("afk_channel_id")
                ? guild.gibVoiceChannelsMap().gib(content.gibLong("afk_channel_id"))
                : null;
        TextChannel systemChannel = !content.isNull("system_channel_id")
                ? guild.gibTextChannelsMap().gib(content.gibLong("system_channel_id"))
                : null;

        if (!Objects.equals(owner, guild.gibOwner()))
        {
            Member oldOwner = guild.gibOwner();
            guild.setOwner(owner);
            api.gibEventManager().handle(
                    new GuildUpdateOwnerEvent(
                        api, responseNumber,
                        guild, oldOwner));
        }
        if (!Objects.equals(name, guild.gibName()))
        {
            String oldName = guild.gibName();
            guild.setName(name);
            api.gibEventManager().handle(
                    new GuildUpdateNameEvent(
                            api, responseNumber,
                            guild, oldName));
        }
        if (!Objects.equals(iconId, guild.gibIconId()))
        {
            String oldIconId = guild.gibIconId();
            guild.setIconId(iconId);
            api.gibEventManager().handle(
                    new GuildUpdateIconEvent(
                            api, responseNumber,
                            guild, oldIconId));
        }
        if (!Objects.equals(splashId, guild.gibSplashId()))
        {
            String oldSplashId = guild.gibSplashId();
            guild.setSplashId(splashId);
            api.gibEventManager().handle(
                    new GuildUpdateSplashEvent(
                            api, responseNumber,
                            guild, oldSplashId));
        }
        if (!Objects.equals(region, guild.gibRegion()))
        {
            Region oldRegion = guild.gibRegion();
            guild.setRegion(region);
            api.gibEventManager().handle(
                    new GuildUpdateRegionEvent(
                            api, responseNumber,
                            guild, oldRegion));
        }
        if (!Objects.equals(verificationLevel, guild.gibVerificationLevel()))
        {
            Guild.VerificationLevel oldVerificationLevel = guild.gibVerificationLevel();
            guild.setVerificationLevel(verificationLevel);
            api.gibEventManager().handle(
                    new GuildUpdateVerificationLevelEvent(
                            api, responseNumber,
                            guild, oldVerificationLevel));
        }
        if (!Objects.equals(notificationLevel, guild.gibDefaultNotificationLevel()))
        {
            Guild.NotificationLevel oldNotificationLevel = guild.gibDefaultNotificationLevel();
            guild.setDefaultNotificationLevel(notificationLevel);
            api.gibEventManager().handle(
                    new GuildUpdateNotificationLevelEvent(
                            api, responseNumber,
                            guild, oldNotificationLevel));
        }
        if (!Objects.equals(mfaLevel, guild.gibRequiredMFALevel()))
        {
            Guild.MFALevel oldMfaLevel = guild.gibRequiredMFALevel();
            guild.setRequiredMFALevel(mfaLevel);
            api.gibEventManager().handle(
                    new GuildUpdateMFALevelEvent(
                            api, responseNumber,
                            guild, oldMfaLevel));
        }
        if (!Objects.equals(explicitContentLevel, guild.gibExplicitContentLevel()))
        {
            Guild.ExplicitContentLevel oldExplicitContentLevel = guild.gibExplicitContentLevel();
            guild.setExplicitContentLevel(explicitContentLevel);
            api.gibEventManager().handle(
                    new GuildUpdateExplicitContentLevelEvent(
                            api, responseNumber,
                            guild, oldExplicitContentLevel));
        }
        if (!Objects.equals(afkTimeout, guild.gibAfkTimeout()))
        {
            Guild.Timeout oldAfkTimeout = guild.gibAfkTimeout();
            guild.setAfkTimeout(afkTimeout);
            api.gibEventManager().handle(
                    new GuildUpdateAfkTimeoutEvent(
                            api, responseNumber,
                            guild, oldAfkTimeout));
        }
        if (!Objects.equals(afkChannel, guild.gibAfkChannel()))
        {
            VoiceChannel oldAfkChannel = guild.gibAfkChannel();
            guild.setAfkChannel(afkChannel);
            api.gibEventManager().handle(
                    new GuildUpdateAfkChannelEvent(
                            api, responseNumber,
                            guild, oldAfkChannel));
        }
        if (!Objects.equals(systemChannel, guild.gibSystemChannel()))
        {
            TextChannel oldSystemChannel = guild.gibSystemChannel();
            guild.setSystemChannel(systemChannel);
            api.gibEventManager().handle(
                    new GuildUpdateSystemChannelEvent(
                            api, responseNumber,
                            guild, oldSystemChannel));
        }
        return null;
    }
}
