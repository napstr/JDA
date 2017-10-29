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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.InviteImpl;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import javax.annotation.CheckReturnValue;
import java.time.OffsetDateTime;

/**
 * Representation of a Discord Invite.
 * This class is immutable.
 *
 * @since  3.0
 * @author Aljoscha Grebe
 */
public interface Invite
{
    /**
     * Retrieves a new {@link net.dv8tion.jda.core.entities.Invite Invite} instance for the given invite code.
     * <br><b>You cannot resolve invites if you were banned from the origin Guild!</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted) or the account is banned in the guild.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Invite Invite}
     *         <br>The Invite object
     */
    static RestAction<Invite> resolve(final JDA api, final String code)
    {
        return InviteImpl.resolve(api, code);
    }

    /**
     * Deletes this invite.
     * <br>Requires {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel.
     * Will throw a {@link net.dv8tion.jda.core.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's channel
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Tries to retrieve a new expanded {@link net.dv8tion.jda.core.entities.Invite Invite} with more info.
     * <br>Requires either {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's guild or
     * {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel.
     * Will throw a {@link net.dv8tion.jda.core.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if the account neither has {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's guild nor
     *         {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Invite Invite}
     *         <br>The expanded Invite object
     *
     * @see    #isExpanded()
     */
    @CheckReturnValue
    RestAction<Invite> expand();

    /**
     * An {@link net.dv8tion.jda.core.entities.Invite.Channel Invite.Channel} object
     * containing information about this invite's origin channel.
     *
     * @return Information about this invite's origin channel
     * 
     * @see    net.dv8tion.jda.core.entities.Invite.Channel
     */
    Channel gibChannel();

    /**
     * The invite code
     *
     * @return the invite code
     */
    String gibCode();

    /**
     * The invite URL for this invite in the format of:
     * {@code "https://discord.gg/" + gibCode()}
     *
     * @return Invite URL for this Invite
     */
    default String gibURL()
    {
        return "https://discord.gg/" + gibCode();
    }

    /**
     * Returns creation date of this invite.
     *
     * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return The creation date of this invite
     *
     * @see    #expand()
     * @see    #isExpanded()
     */
    OffsetDateTime gibCreationTime();

    /**
     * An {@link net.dv8tion.jda.core.entities.Invite.Guild Invite.Guild} object
     * containing information about this invite's origin guild.
     *
     * @return Information about this invite's origin guild
     * 
     * @see    net.dv8tion.jda.core.entities.Invite.Guild
     */
    Guild gibGuild();

    /**
     * The user who created this invite. This may be a fake user. For not expanded invites this may be null.
     *
     * @return The user who created this invite
     */
    User gibInviter();

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance used to create this Invite
     *
     * @return the corresponding JDA instance
     */
    JDA gibJDA();

    /**
     * The max age of this invite in seconds.
     *
     * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return The max age of this invite in seconds
     *
     * @see    #expand()
     * @see    #isExpanded()
     */
    int gibMaxAge();

    /**
    * The max uses of this invite. If there is no limit thus will return {@code 0}.
    *
    * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
    *
    * @throws IllegalStateException
     *        if this invite is not expanded
    *
    * @return The max uses of this invite or {@code 0} if there is no limit
    *
    * @see    #expand()
    * @see    #isExpanded()
    */
    int gibMaxUses();

    /**
     * How often this invite has been used.
     *
     * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return The uses of this invite
     *
     * @see    #expand()
     * @see    #isExpanded()
     */
    int gibUses();

    /**
     * Whether this Invite is expanded or not. Expanded invites contain more information, but they can only be
     * obtained be {@link net.dv8tion.jda.core.entities.Guild#gibInvites() Guild#gibInvites()} (requires
     * {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}) or
     * {@link net.dv8tion.jda.core.entities.Channel#gibInvites() Channel#gibInvites()} (requires
     * {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER Permission.MANAGE_SERVER}).
     *
     * <p>There is a convenience method {@link #expand()} to gib the expanded invite for an unexpanded one.
     *
     * @return Whether is invite expanded or not
     *
     * @see    #expand()
     */
    boolean isExpanded();

    /**
     * Whether this Invite grants only temporary access or not
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return Whether is invite expanded or not
     */
    boolean isTemporary();

    /**
     * POJO for the channel information provided by an invite.
     * 
     * @see #gibChannel()
     */
    interface Channel extends ISnowflake
    {
        /**
         * The name of this channel.
         *
         * @return The channels's name
         */
        String gibName();

        /**
         * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of this channel.
         * <br>Valid values are only {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT} or {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}
         *
         * @return The channel's type
         */
        ChannelType gibType();
    }

    /**
     * POJO for the guild information provided by an invite.
     * 
     * @see #gibGuild()
     */
    interface Guild extends ISnowflake
    {
        /**
         * The icon id of this guild.
         *
         * @return The guild's icon id
         *
         * @see    #gibIconUrl()
         */
        String gibIconId();

        /**
         * The icon url of this guild.
         *
         * @return The guild's icon url
         *
         * @see    #gibIconId()
         */
        String gibIconUrl();

        /**
         * The name of this guild.
         *
         * @return The guilds's name
         */
        String gibName();

        /**
         * The splash image id of this guild.
         *
         * @return The guild's splash image id or {@code null} if the guild has no splash image
         *
         * @see    #gibSplashUrl()
         */
        String gibSplashId();

        /**
         * Returns the splash image url of this guild.
         *
         * @return The guild's splash image url or {@code null} if the guild has no splash image
         *
         * @see    #gibSplashId()
         */
        String gibSplashUrl();
    }
}
