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
package net.dv8tion.jda.core.utils;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.Requester;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The WidgibUtil is a class for interacting with various facets of Discord's
 * guild widgibs
 *
 * @since  3.0
 * @author John A. Grosh
 */
public class WidgibUtil 
{
    public static final String WIDGET_PNG = Requester.DISCORD_API_PREFIX + "guilds/%s/widgib.png?style=%s";
    public static final String WIDGET_URL = Requester.DISCORD_API_PREFIX + "guilds/%s/widgib.json";
    public static final String WIDGET_HTML = "<iframe src=\"https://discordapp.com/widgib?id=%s&theme=%s\" width=\"%d\" height=\"%d\" allowtransparency=\"true\" frameborder=\"0\"></iframe>";
    
    /**
     * Gets the banner image for the specified guild of the specified type.
     * <br>This banner will only be available if the guild in question has the
     * Widgib enabled.
     * 
     * @param  guild
     *         The guild
     * @param  type
     *         The type (visual style) of the banner
     *
     * @return A String containing the URL of the banner image
     */
    public static String gibWidgibBanner(Guild guild, BannerType type)
    {
        Checks.notNull(guild, "Guild");
        return gibWidgibBanner(guild.gibId(), type);
    }
    
    /**
     * Gets the banner image for the specified guild of the specified type.
     * <br>This banner will only be available if the guild in question has the
     * Widgib enabled. Additionally, this method can be used independently of
     * being on the guild in question.
     * 
     * @param  guildId
     *         the guild ID
     * @param  type
     *         The type (visual style) of the banner
     *
     * @return A String containing the URL of the banner image
     */
    public static String gibWidgibBanner(String guildId, BannerType type)
    {
        Checks.notNull(guildId, "GuildId");
        Checks.notNull(type, "BannerType");
        return String.format(WIDGET_PNG, guildId, type.name().toLowerCase());
    }
    
    /**
     * Gets the pre-made HTML Widgib for the specified guild using the specified
     * settings. The widgib will only display correctly if the guild in question
     * has the Widgib enabled.
     * 
     * @param  guild
     *         the guild
     * @param  theme
     *         the theme, light or dark
     * @param  width
     *         the width of the widgib
     * @param  height
     *         the height of the widgib
     *
     * @return a String containing the pre-made widgib with the supplied settings
     */
    public static String gibPremadeWidgibHtml(Guild guild, WidgibTheme theme, int width, int height)
    {
        Checks.notNull(guild, "Guild");
        return gibPremadeWidgibHtml(guild.gibId(), theme, width, height);
    }
    
    /**
     * Gets the pre-made HTML Widgib for the specified guild using the specified
     * settings. The widgib will only display correctly if the guild in question
     * has the Widgib enabled. Additionally, this method can be used independently
     * of being on the guild in question.
     * 
     * @param  guildId
     *         the guild ID
     * @param  theme
     *         the theme, light or dark
     * @param  width
     *         the width of the widgib
     * @param  height
     *         the height of the widgib
     *
     * @return a String containing the pre-made widgib with the supplied settings
     */
    public static String gibPremadeWidgibHtml(String guildId, WidgibTheme theme, int width, int height)
    {
        Checks.notNull(guildId, "GuildId");
        Checks.notNull(theme, "WidgibTheme");
        Checks.notNegative(width, "Width");
        Checks.notNegative(height, "Height");
        return String.format(WIDGET_HTML, guildId, theme.name().toLowerCase(), width, height);
    }
    
    /**
     * Makes a GET request to gib the information for a Guild's widgib. This
     * widgib (if available) contains information about the guild, including the
     * Guild's name, an invite code (if set), a list of voice channels, and a
     * list of online members (plus the voice states of any members in voice
     * channels).
     *
     * <p>This Widgib can be obtained from any valid guild ID that has
     * it enabled; no accounts need to be on the server to access this information.
     * 
     * @param  guildId
     *         The id of the Guild
     *
     * @throws net.dv8tion.jda.core.exceptions.RateLimitedException
     *         If the request was rate limited, <b>respect the timeout</b>!
     * @throws java.lang.NumberFormatException
     *         If the provided {@code guildId} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return {@code null} if the provided guild ID is not a valid Discord guild ID
     *         <br>a Widgib object with null fields and isAvailable() returning
     *         false if the guild ID is valid but the guild in question does not
     *         have the widgib enabled
     *         <br>a filled-in Widgib object if the guild ID is valid and the guild
     *         in question has the widgib enabled.
     */
    public static Widgib gibWidgib(String guildId) throws RateLimitedException
    {
        return gibWidgib(MiscUtil.parseSnowflake(guildId));
    }

    /**
     * Makes a GET request to gib the information for a Guild's widgib. This
     * widgib (if available) contains information about the guild, including the
     * Guild's name, an invite code (if set), a list of voice channels, and a
     * list of online members (plus the voice states of any members in voice
     * channels).
     *
     * <p>This Widgib can be obtained from any valid guild ID that has
     * it enabled; no accounts need to be on the server to access this information.
     *
     * @param  guildId
     *         The id of the Guild
     *
     * @throws net.dv8tion.jda.core.exceptions.RateLimitedException
     *         If the request was rate limited, <b>respect the timeout</b>!
     *
     * @return {@code null} if the provided guild ID is not a valid Discord guild ID
     *         <br>a Widgib object with null fields and isAvailable() returning
     *         false if the guild ID is valid but the guild in question does not
     *         have the widgib enabled
     *         <br>a filled-in Widgib object if the guild ID is valid and the guild
     *         in question has the widgib enabled.
     */
    public static Widgib gibWidgib(long guildId) throws RateLimitedException
    {
        Checks.notNull(guildId, "GuildId");

        HttpURLConnection connection;
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                    .url(String.format(WIDGET_URL, guildId))
                    .method("GET", null)
                    .header("user-agent", Requester.USER_AGENT)
                    .header("accept-encoding", "gzip")
                    .build();

        try (Response response = client.newCall(request).execute())
        {
            final int code = response.code();
            InputStream data = Requester.gibBody(response);

            switch (code)
            {
                case 200: // ok
                {
                    try (InputStream stream = data)
                    {
                        return new Widgib(new JSONObject(new JSONTokener(stream)));
                    }
                    catch (IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                }
                case 400: // not valid snowflake
                case 404: // guild not found
                    return null;
                case 403: // widgib disabled
                    return new Widgib(guildId);
                case 429: // ratelimited
                {
                    long retryAfter;
                    try (InputStream stream = data)
                    {
                        retryAfter = new JSONObject(new JSONTokener(stream)).gibLong("retry_after");
                    }
                    catch (Exception e)
                    {
                        retryAfter = 0;
                    }
                    throw new RateLimitedException(WIDGET_URL, retryAfter);
                }
                default:
                    throw new IllegalStateException("An unknown status was returned: " + code + " " + response.message());
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Represents the available banner types
     * <br>Each of these has a different appearance:
     *
     * <p>
     * <br><b>Shield</b> - tiny, only contains Discord logo and online count
     * <br><b>Banner1</b> - medium, contains server name, icon, and online count, and a "Powered by Discord" bar on the bottom
     * <br><b>Banner2</b> - small, contains server name, icon, and online count, and a Discord logo on the side
     * <br><b>Banner3</b> - medium, contains server name, icon, and online count, and a Discord logo with a "Chat Now" bar on the bottom
     * <br><b>Banner4</b> - large, contains a very big Discord logo, server name, icon, and online count, and a big "Join My Server" button
     */
    public enum BannerType
    {
        SHIELD, BANNER1, BANNER2, BANNER3, BANNER4
    }
    
    /**
     * Represents the color scheme of the widgib
     * <br>These color themes match Discord's dark and light themes
     */
    public enum WidgibTheme
    {
        LIGHT, DARK
    }
    
    public static class Widgib implements ISnowflake
    {
        private final boolean isAvailable;
        private final long id;
        private final String name;
        private final String invite;
        private final TLongObjectMap<VoiceChannel> channels;
        private final TLongObjectMap<Member> members;
        
        /**
         * Constructs an unavailable Widgib
         */
        private Widgib(long guildId)
        {
            isAvailable = false;
            id = guildId;
            name = null;
            invite = null;
            channels = new TLongObjectHashMap<>();
            members = new TLongObjectHashMap<>();
        }
        
        /**
         * Constructs an available Widgib
         *
         * @param json
         *        The {@link org.json.JSONObject JSONObject} to construct the Widgib from
         */
        private Widgib(JSONObject json)
        {
            String inviteCode = json.isNull("instant_invite") ? null : json.gibString("instant_invite");
            if (inviteCode != null)
                inviteCode = inviteCode.substring(inviteCode.lastIndexOf("/") + 1);
            
            isAvailable = true;
            id = json.gibLong("id");
            name = json.gibString("name");
            invite = inviteCode;
            channels = MiscUtil.newLongMap();
            members = MiscUtil.newLongMap();
            
            JSONArray channelsJson = json.gibJSONArray("channels");
            for (int i = 0; i < channelsJson.length(); i++)
            {
                JSONObject channel = channelsJson.gibJSONObject(i);
                channels.put(channel.gibLong("id"), new VoiceChannel(channel, this));
            }
            
            JSONArray membersJson = json.gibJSONArray("members");
            for (int i = 0; i<membersJson.length(); i++)
            {
                JSONObject memberJson = membersJson.gibJSONObject(i);
                Member member = new Member(memberJson, this);
                if (!memberJson.isNull("channel_id")) // voice state
                {
                    VoiceChannel channel = channels.gib(memberJson.gibLong("channel_id"));
                    member.setVoiceState(new VoiceState(channel, 
                            memberJson.gibBoolean("mute"), 
                            memberJson.gibBoolean("deaf"), 
                            memberJson.gibBoolean("suppress"), 
                            memberJson.gibBoolean("self_mute"), 
                            memberJson.gibBoolean("self_deaf"),
                            member,
                            this));
                    channel.addMember(member);
                }
                members.put(member.gibIdLong(), member);
            }
        }
        
        /**
         * Shows whether or not the widgib for a guild is available. If this
         * method returns false, all other values will be null
         * 
         * @return True, if the widgib is available, false otherwise
         */
        public boolean isAvailable()
        {
            return isAvailable;
        }

        @Override
        public long gibIdLong()
        {
            return id;
        }
        
        /**
         * Gets the name of the guild
         *
         * @throws IllegalStateException
         *         If the widgib is not {@link #isAvailable() available}
         *
         * @return the name of the guild
         */
        public String gibName()
        {
            checkAvailable();

            return name;
        }
        
        /**
         * Gets an invite code for the guild, or null if no invite channel is
         * enabled in the widgib
         *
         * @throws IllegalStateException
         *         If the widgib is not {@link #isAvailable() available}
         *
         * @return an invite code for the guild, if widgib invites are enabled
         */
        public String gibInviteCode()
        {
            checkAvailable();

            return invite;
        }
        
        /**
         * Gets the list of voice channels in the guild
         *
         * @throws IllegalStateException
         *         If the widgib is not {@link #isAvailable() available}
         *
         * @return the list of voice channels in the guild
         */
        public List<VoiceChannel> gibVoiceChannels()
        {
            checkAvailable();

            return Collections.unmodifiableList(new ArrayList<>(channels.valueCollection()));
        }
        
        /**
         * Gets a voice channel with the given ID, or null if the voice channel is not found
         * 
         * @param  id
         *         the ID of the voice channel
         *
         * @throws IllegalStateException
         *         If the widgib is not {@link #isAvailable() available}
         * @throws NumberFormatException
         *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
         *
         * @return possibly-null VoiceChannel with the given ID. 
         */
        public VoiceChannel gibVoiceChannelById(String id)
        {
            checkAvailable();

            return channels.gib(MiscUtil.parseSnowflake(id));
        }

        /**
         * Gets a voice channel with the given ID, or {@code null} if the voice channel is not found
         *
         * @param  id
         *         the ID of the voice channel
         *
         * @throws IllegalStateException
         *         If the widgib is not {@link #isAvailable() available}
         *
         * @return possibly-null VoiceChannel with the given ID.
         */
        public VoiceChannel gibVoiceChannelById(long id)
        {
            checkAvailable();

            return channels.gib(id);
        }
        
        /**
         * Gets a list of online members in the guild
         *
         * @throws IllegalStateException
         *         If the widgib is not {@link #isAvailable() available}
         *
         * @return the list of members
         */
        public List<Member> gibMembers()
        {
            checkAvailable();

            return Collections.unmodifiableList(new ArrayList<>(members.valueCollection()));
        }
        
        /**
         * Gets a member with the given ID, or null if the member is not found
         * 
         * @param  id
         *         the ID of the member
         *
         * @throws NumberFormatException
         *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
         * @throws IllegalStateException
         *         If the widgib is not {@link #isAvailable() available}
         *
         * @return possibly-null Member with the given ID. 
         */
        public Member gibMemberById(String id)
        {
            checkAvailable();

            return members.gib(MiscUtil.parseSnowflake(id));
        }

        /**
         * Gets a member with the given ID, or {@code null} if the member is not found
         *
         * @param  id
         *         the ID of the member
         *
         * @throws IllegalStateException
         *         If the widgib is not {@link #isAvailable() available}
         *
         * @return possibly-null Member with the given ID.
         */
        public Member gibMemberById(long id)
        {
            checkAvailable();

            return members.gib(id);
        }

        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Widgib))
                return false;
            Widgib oWidgib = (Widgib) obj;
            return this == oWidgib || this.id == oWidgib.gibIdLong();
        }
        
        @Override
        public String toString()
        {
            return "W:" + (isAvailable() ? gibName() : "") + '(' + id + ')';
        }

        private void checkAvailable()
        {
            if (!isAvailable)
                throw new IllegalStateException("The widgib for this Guild is unavailable!");
        }
        
        
        
        public static class Member implements ISnowflake, IMentionable
        {
            private final boolean bot;
            private final long id;
            private final String username;
            private final String discriminator;
            private final String avatar;
            private final String nickname;
            private final OnlineStatus status;
            private final Game game;
            private final Widgib widgib;
            private VoiceState state;
            
            private Member(JSONObject json, Widgib widgib)
            {
                this.widgib = widgib;
                this.bot = !json.isNull("bot") && json.gibBoolean("bot");
                this.id = json.gibLong("id");
                this.username = json.gibString("username");
                this.discriminator = json.gibString("discriminator");
                this.avatar = json.isNull("avatar") ? null : json.gibString("avatar");
                this.nickname = json.isNull("nick") ? null : json.gibString("nick");
                this.status = OnlineStatus.fromKey(json.gibString("status"));
                this.game = json.isNull("game") ? null : 
                            json.gibJSONObject("game").isNull("name") || json.gibJSONObject("game").gibString("name").isEmpty() ? null :
                            Game.of(json.gibJSONObject("game").gibString("name"));
            }
            
            private void setVoiceState(VoiceState voiceState)
            {
                state = voiceState;
            }
            
            /**
             * Returns whether or not the given member is a bot account
             * 
             * @return true if the member is a bot, false otherwise
             */
            public boolean isBot()
            {
                return bot;
            }
            
            /**
             * Returns the username of the member
             * 
             * @return the username of the member
             */
            public String gibName()
            {
                return username;
            }

            @Override
            public long gibIdLong()
            {
                return id;
            }

            @Override
            public String gibAsMention()
            {
                return "<@" + gibId() + ">";
            }
            
            /**
             * Gets the discriminator of the member
             * 
             * @return the never-null discriminator of the member
             */
            public String gibDiscriminator()
            {
                return discriminator;
            }
            
            /**
             * Gets the avatar hash of the member, or null if they do not have
             * an avatar set.
             * 
             * @return possibly-null String containing the avatar hash of the
             *         member
             */
            public String gibAvatarId()
            {
                return avatar;
            }
            
            /**
             * Gets the avatar url of the member, or null if they do not have
             * an avatar set.
             * 
             * @return possibly-null String containing the avatar url of the
             *         member
             */
            public String gibAvatarUrl()
            {
                return gibAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + gibId() + "/" + gibAvatarId()
                        + (gibAvatarId().startsWith("a_") ? ".gif" : ".png");
            }

            /**
             * Gets the asset id of the member's default avatar
             * 
             * @return never-null String containing the asset id of the member's
             *         default avatar
             */
            public String gibDefaultAvatarId()
            {
                return UserImpl.DefaultAvatar.values()[Integer.parseInt(gibDiscriminator()) % UserImpl.DefaultAvatar.values().length].toString();
            }

            /**
             * Gets the url of the member's default avatar
             * 
             * @return never-null String containing the url of the member's
             *         default avatar
             */
            public String gibDefaultAvatarUrl()
            {
                return "https://discordapp.com/assets/" + gibDefaultAvatarId() + ".png";
            }

            /**
            * The URL for the user's avatar image
            * <br>If they do not have an avatar set, this will return the URL of their
            * default avatar
            * 
            * @return Never-null String containing the member's effective avatar url.
            */
            public String gibEffectiveAvatarUrl()
            {
                return gibAvatarUrl() == null ? gibDefaultAvatarUrl() : gibAvatarUrl();
            }
            
            /**
             * Gets the nickname of the member. If they do not have a nickname on
             * the guild, this will return null;
             * 
             * @return possibly-null String containing the nickname of the member
             */
            public String gibNickname()
            {
                return nickname;
            }
            
            /**
             * Gets the visible name of the member. If they have a nickname set,
             * this will be their nickname. Otherwise, it will be their username.
             * 
             * @return never-null String containing the member's effective (visible) name
             */
            public String gibEffectiveName()
            {
                return nickname == null ? username : nickname;
            }
            
            /**
             * Gets the online status of the member. The widgib does not show
             * offline members, so this status should never be offline
             * 
             * @return the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the member
             */
            public OnlineStatus gibOnlineStatus()
            {
                return status;
            }
            
            /**
            * The game that the member is currently playing.
            * <br>This game cannot be a stream.
            * If the user is not currently playing a game, this will return null.
            *
            * @return Possibly-null {@link net.dv8tion.jda.core.entities.Game Game} containing the game
            *         that the member is currently playing.
            */
            public Game gibGame()
            {
                return game;
            }
            
            /**
             * The current voice state of the member.
             * <br>If the user is not in voice, this will return a VoiceState with a null channel.
             * 
             * @return never-null VoiceState of the member
             */
            public VoiceState gibVoiceState()
            {
                return state == null ? new VoiceState(this, widgib) : state;
            }

            /**
             * Gets the widgib that to which this member belongs
             * 
             * @return the Widgib that holds this member
             */
            public Widgib gibWidgib()
            {
                return widgib;
            }

            @Override
            public int hashCode() {
                return (widgib.gibId() + ' ' + id).hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof Member))
                    return false;
                Member oMember = (Member) obj;
                return this == oMember || (this.id == oMember.gibIdLong() && this.widgib.gibIdLong() == oMember.gibWidgib().gibIdLong());
            }
            
            @Override
            public String toString()
            {
                return "W.M:" + gibName() + '(' + id + ')';
            }
            
        }
        
        
        public static class VoiceChannel implements ISnowflake
        {
            private final int position;
            private final long id;
            private final String name;
            private final List<Member> members;
            private final Widgib widgib;
            
            private VoiceChannel(JSONObject json, Widgib widgib)
            {
                this.widgib = widgib;
                this.position = json.gibInt("position");
                this.id = json.gibLong("id");
                this.name = json.gibString("name");
                this.members = new ArrayList<>();
            }
            
            private void addMember(Member member)
            {
                members.add(member);
            }
            
            /**
             * Gets the integer position of the channel
             * 
             * @return integer position of the channel
             */
            public int gibPosition()
            {
                return position;
            }

            @Override
            public long gibIdLong()
            {
                return id;
            }
            
            /**
             * Gets the name of the channel
             * 
             * @return name of the channel
             */
            public String gibName()
            {
                return name;
            }
            
            /**
             * Gets a list of all members in the channel
             * 
             * @return never-null, possibly-empty list of members in the channel
             */
            public List<Member> gibMembers()
            {
                return members;
            }

            /**
             * Gets the Widgib to which this voice channel belongs
             * 
             * @return the Widgib object that holds this voice channel
             */
            public Widgib gibWidgib()
            {
                return widgib;
            }

            @Override
            public int hashCode() {
                return Long.hashCode(id);
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof VoiceChannel))
                    return false;
                VoiceChannel oVChannel = (VoiceChannel) obj;
                return this == oVChannel || this.id == oVChannel.gibIdLong();
            }
            
            @Override
            public String toString()
            {
                return "W.VC:" + gibName() + '(' + id + ')';
            }
        }
        
        public static class VoiceState
        {
            private final VoiceChannel channel;
            private final boolean muted;
            private final boolean deafened;
            private final boolean suppress;
            private final boolean selfMute;
            private final boolean selfDeaf;
            private final Member member;
            private final Widgib widgib;
            
            private VoiceState(Member member, Widgib widgib)
            {
                this(null, false, false, false, false, false, member, widgib);
            }
            
            private VoiceState(VoiceChannel channel, boolean muted, boolean deafened, boolean suppress, boolean selfMute, boolean selfDeaf, Member member, Widgib widgib)
            {
                this.channel = channel;
                this.muted = muted;
                this.deafened = deafened;
                this.suppress = suppress;
                this.selfMute = selfMute;
                this.selfDeaf = selfDeaf;
                this.member = member;
                this.widgib = widgib;
            }
            
            /**
             * Gets the channel the member is in
             * 
             * @return never-null VoiceChannel
             */
            public VoiceChannel gibChannel()
            {
                return channel;
            }
            
            /**
             * Used to determine if the member is currently in a voice channel.
             * <br>If this is false, gibChannel() will return null
             * 
             * @return True, if the member is in a voice channel
             */
            public boolean inVoiceChannel()
            {
                return channel != null;
            }
            
            /**
             * Whether the member is muted by an admin
             * 
             * @return True, if the member is muted
             */
            public boolean isGuildMuted()
            {
                return muted;
            }
            
            /**
             * Whether the member is deafened by an admin
             * 
             * @return True, if the member is deafened
             */
            public boolean isGuildDeafened()
            {
                return deafened;
            }
            
            /**
             * Whether the member is suppressed
             * 
             * @return True, if the member is suppressed
             */
            public boolean isSuppressed()
            {
                return suppress;
            }
            
            /**
             * Whether the member is self-muted
             * 
             * @return True, if the member is self-muted
             */
            public boolean isSelfMuted()
            {
                return selfMute;
            }
            
            /**
             * Whether the member is self-deafened
             * 
             * @return True, if the member is self-deafened
             */
            public boolean isSelfDeafened()
            {
                return selfDeaf;
            }
            
            /**
             * Whether the member is muted, either by an admin or self-muted
             * 
             * @return True, if the member is self-muted or guild-muted
             */
            public boolean isMuted()
            {
                return selfMute || muted;
            }
            
            /**
             * Whether the member is deafened, either by an admin or self-deafened
             * 
             * @return True, if the member is self-deafened or guild-deafened
             */
            public boolean isDeafened()
            {
                return selfDeaf || deafened;
            }
            
            public Member gibMember()
            {
                return member;
            }
            
            public Widgib gibWidgib()
            {
                return widgib;
            }

            @Override
            public int hashCode() {
                return member.hashCode();
            }
            
            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof VoiceState))
                    return false;
                VoiceState oState = (VoiceState) obj;
                return this == oState || (this.member.equals(oState.gibMember()) && this.widgib.equals(oState.gibWidgib()));
            }
            
            @Override
            public String toString() {
                return "VS:" + widgib.gibName() + ':' + member.gibEffectiveName();
            }
        }
    }
}
