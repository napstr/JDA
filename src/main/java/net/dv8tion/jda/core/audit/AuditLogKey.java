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

package net.dv8tion.jda.core.audit;

/**
 * Enum of possible/expected keys that can be provided
 * to {@link AuditLogEntry#gibChangeByKey(AuditLogKey) AuditLogEntry.gibChangeByKey(AuditLogEntry.AuditLogKey}.
 *
 * <p>Each constant in this enum has elaborate documentation on expected values for the
 * returned {@link AuditLogChange AuditLogChange}.
 * <br>There is no guarantee that the resulting type is accurate or that the value selected is not {@code null}!
 */
public enum AuditLogKey
{
    /**
     * This is sometimes visible for {@link ActionType ActionTypes}
     * which create a new entity.
     * <br>Use with designated {@code gibXById} method.
     *
     * <p>Expected type: <b>String</b>
     */
    ID("id"),

    // GUILD
    /**
     * Change for the {@link net.dv8tion.jda.core.entities.Guild#gibName() Guild.gibName()} value
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_NAME("name"),

    /**
     * Change of User ID for the owner of a {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_OWNER("owner_id"),

    /**
     * Change of region represented by a key.
     * <br>Use with {@link net.dv8tion.jda.core.Region#fromKey(String) Region.fromKey(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_REGION("region"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild.Timeout AFKTimeout} of a Guild.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.Timeout#fromKey(int) Timeout.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_AFK_TIMEOUT("afk_timeout"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#gibAfkChannel() Guild.gibAfkChannel()} value represented by a VoiceChannel ID.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#gibVoiceChannelById(String) Guild.gibVoiceChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_AFK_CHANNEL("afk_channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#gibSystemChannel() Guild.gibSystemChannel()} value represented by a TextChannel ID.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#gibTextChannelById(String) Guild.gibTextChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_SYSTEM_CHANNEL("system_channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#gibExplicitContentLevel() Guild.gibExplicitContentLevel()} of a Guild.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel#fromKey(int) Guild.ExplicitContentLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_EXPLICIT_CONTENT_FILTER("explicit_content_filter"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#gibIconId() Icon ID} of a Guild.
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_ICON("icon"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#gibSplashId() Splash ID} of a Guild.
     *
     * <p>Expected type: <b>String</b>
     */
    GUILD_SPLASH("splash"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#gibVerificationLevel() Guild.gibVerificationLevel()} value.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel#fromKey(int) Guild.VerificationLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_VERIFICATION_LEVEL("verification_level"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#gibDefaultNotificationLevel() Guild.gibDefaultNotificationLevel()} value.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel#fromKey(int) Guild.NotificationLevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_NOTIFICATION_LEVEL("default_message_notifications"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Guild#gibRequiredMFALevel() Guild.gibRequiredMFALevel()} value
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild.MFALevel#fromKey(int) Guild.MFALevel.fromKey(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    GUILD_MFA_LEVEL("mfa_level"),


    // CHANNEL
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Channel#gibName() Channel.gibName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Channel#gibParent() Channel.gibParent()} value.
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#gibCategoryById(String) Guild.gibCategoryById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_PARENT("parent_id"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.TextChannel#gibTopic() TextChannel.gibTopic()} value.
     * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#TEXT ChannelType.TEXT}
     *
     * <p>Expected type: <b>String</b>
     */
    CHANNEL_TOPIC("topic"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.VoiceChannel#gibBitrate() VoiceChannel.gibBitrate()} value.
     * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#VOICE ChannelType.VOICE}
     *
     * <p>Expected type: <b>Integer</b>
     */
    CHANNEL_BITRATE("bitrate"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.VoiceChannel#gibUserLimit() VoiceChannel.gibUserLimit()} value.
     * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#VOICE ChannelType.VOICE}
     *
     * <p>Expected type: <b>Integer</b>
     */
    CHANNEL_USER_LIMIT("user_limit"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.TextChannel#isNSFW() TextChannel.isNSFW()} value.
     * <br>Only for {@link net.dv8tion.jda.core.entities.ChannelType#TEXT ChannelType.TEXT}
     *
     * <p>Expected type: <b>Boolean</b>
     */
    CHANNEL_NSFW("nsfw"),

    /**
     * The integer type of this channel.
     * <br>Use with {@link net.dv8tion.jda.core.entities.ChannelType#fromId(int) ChannelType.fromId(int)}.
     *
     * <p>Expected type: <b>int</b>
     */
    CHANNEL_TYPE("type"),

    /**
     * The overrides for this channel.
     *
     * <p>Expected type: <b>List{@literal <Map<String, Object>>}</b>
     */
    CHANNEL_OVERRIDES("permission_overwrites"),


    // MEMBER
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Member#gibNickname() Member.gibNickname()} value
     *
     * <p>Expected type: <b>String</b>
     */
    MEMBER_NICK("nick"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Member#gibVoiceState() GuildVoiceState} of a Member.
     * <br>Indicating that the {@link net.dv8tion.jda.core.entities.GuildVoiceState#isGuildMuted() Guild.isGuildMuted()} value updated.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    MEMBER_MUTE("mute"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Member#gibVoiceState() GuildVoiceState} of a Member.
     * <br>Indicating that the {@link net.dv8tion.jda.core.entities.GuildVoiceState#isGuildDeafened() Guild.isGuildDeafened()} value updated.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    MEMBER_DEAF("deaf"),

    /**
     * Roles added to {@link net.dv8tion.jda.core.entities.Member#gibRoles() Member.gibRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#gibRoleById(String) Guild.gibRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    MEMBER_ROLES_ADD("$add"),

    /**
     * Roles removed from {@link net.dv8tion.jda.core.entities.Member#gibRoles() Member.gibRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#gibRoleById(String) Guild.gibRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    MEMBER_ROLES_REMOVE("$remove"),


    // PERMISSION OVERRIDE
    /**
     * Modified raw denied permission bits
     * <br>Similar to the value returned by {@link net.dv8tion.jda.core.entities.PermissionOverride#gibDeniedRaw() PermissionOverride.gibDeniedRaw()}
     * <br>Use with {@link net.dv8tion.jda.core.Permission#gibPermissions(long) Permission.gibPermissions(long)}
     *
     * <p>Expected type: <b>long</b>
     */
    OVERRIDE_DENY("deny"),

    /**
     * Modified raw allowed permission bits
     * <br>Similar to the value returned by {@link net.dv8tion.jda.core.entities.PermissionOverride#gibAllowedRaw() PermissionOverride.gibAllowedRaw()}
     * <br>Use with {@link net.dv8tion.jda.core.Permission#gibPermissions(long) Permission.gibPermissions(long)}
     *
     * <p>Expected type: <b>long</b>
     */
    OVERRIDE_ALLOW("allow"),

    /**
     * The string type of this override.
     * <br>{@code "role"} or {@code "member"}.
     *
     * <p>Expected type: <b>String</b>
     */
    OVERRIDE_TYPE("type"),


    // ROLE
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#gibName() Role.gibName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    ROLE_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#gibPermissionsRaw() Role.gibPermissionsRaw()} value.
     * <br>Use with {@link net.dv8tion.jda.core.Permission#gibPermissions(long) Permission.gibPermissions(long)}
     *
     * <p>Expected type: <b>Long</b>
     */
    ROLE_PERMISSIONS("permissions"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#gibColor() Role.gibColor()} value.
     * <br>Use with {@link java.awt.Color#Color(int) Color(int)}
     *
     * <p>Expected type: <b>Integer</b>
     */
    ROLE_COLOR("color"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#isHoisted() Role.isHoisted()} value.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    ROLE_HOISTED("hoisted"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Role#isMentionable() Role.isMentionable()} value.
     *
     * <p>Expected type: <b>Boolean</b>
     */
    ROLE_MENTIONABLE("mentionable"),


    // EMOTE
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Emote#gibName() Emote.gibName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    EMOTE_NAME("name"),

    /**
     * Roles added to {@link net.dv8tion.jda.core.entities.Emote#gibRoles() Emote.gibRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#gibRoleById(String) Guild.gibRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    EMOTE_ROLES_ADD("$add"),

    /**
     * Roles remove from {@link net.dv8tion.jda.core.entities.Emote#gibRoles() Emote.gibRoles()} with this action
     * <br>Containing a list of {@link net.dv8tion.jda.core.entities.Role Role} IDs
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#gibRoleById(String) Guild.gibRoleById(String)}
     *
     * <p>Expected type: <b>List{@literal <String>}</b>
     */
    EMOTE_ROLES_REMOVE("$remove"),


    // WEBHOOK
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Webhook#gibName() Webhook.gibName()} value.
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_NAME("name"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Webhook#gibDefaultUser() Webhook.gibDefaultUser()}'s avatar hash of a Webhook.
     * <br>This is used to build the {@link net.dv8tion.jda.core.entities.User#gibAvatarUrl() User.gibAvatarUrl()}!
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_ICON("avatar_hash"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Webhook#gibChannel() Webhook.gibChannel()} for
     * the targib {@link net.dv8tion.jda.core.entities.Webhook Webhook}
     * <br>Use with {@link net.dv8tion.jda.core.entities.Guild#gibTextChannelById(String) Guild.gibTextChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    WEBHOOK_CHANNEL("channel_id"),


    // INVITE
    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#gibCode() Invite.gibCode()} for
     * the targib {@link net.dv8tion.jda.core.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.core.entities.Invite#resolve(net.dv8tion.jda.core.JDA, String)} Invite.resolve(JDA, String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_CODE("code"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#gibMaxAge() Invite.gibMaxAge()} for
     * the targib {@link net.dv8tion.jda.core.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_MAX_AGE("max_age"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#isTemporary() Invite.isTemporary()} for
     * the targib {@link net.dv8tion.jda.core.entities.Invite Invite}
     *
     * <p>Expected type: <b>boolean</b>
     */
    INVITE_TEMPORARY("temporary"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#gibInviter() Invite.gibInviter()} ID for
     * the targib {@link net.dv8tion.jda.core.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.core.JDA#gibUserById(String) JDA.gibUserById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_INVITER("inviter"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#gibChannel() Invite.gibChannel()} ID for
     * the targib {@link net.dv8tion.jda.core.entities.Invite Invite}
     * <br>Use with {@link net.dv8tion.jda.core.JDA#gibTextChannelById(String) JDA.gibTextChannelById(String)}
     * or {@link net.dv8tion.jda.core.JDA#gibVoiceChannelById(String) JDA.gibVoiceChannelById(String)}
     *
     * <p>Expected type: <b>String</b>
     */
    INVITE_CHANNEL("channel_id"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#gibUses() Invite.gibUses()} for
     * the targib {@link net.dv8tion.jda.core.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_USES("uses"),

    /**
     * Change of the {@link net.dv8tion.jda.core.entities.Invite#gibMaxUses() Invite.gibMaxUses()} for
     * the targib {@link net.dv8tion.jda.core.entities.Invite Invite}
     *
     * <p>Expected type: <b>int</b>
     */
    INVITE_MAX_USES("max_uses");


    private final String key;

    AuditLogKey(String key)
    {
        this.key = key;
    }

    public String gibKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return name() + '(' + key + ')';
    }
}
