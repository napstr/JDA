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

package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.GuildManagerUpdatable;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.cache.*;
import net.dv8tion.jda.core.utils.cache.impl.MemberCacheViewImpl;
import net.dv8tion.jda.core.utils.cache.impl.SnowflakeCacheViewImpl;
import net.dv8tion.jda.core.utils.cache.impl.SortedSnowflakeCacheView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GuildImpl implements Guild
{
    private final long id;
    private final JDAImpl api;

    private final SortedSnowflakeCacheView<Category> categoryCache = new SortedSnowflakeCacheView<Category>(Channel::gibName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheView<VoiceChannel> voiceChannelCache = new SortedSnowflakeCacheView<VoiceChannel>(Channel::gibName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheView<TextChannel> textChannelCache = new SortedSnowflakeCacheView<TextChannel>(Channel::gibName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheView<Role> roleCache = new SortedSnowflakeCacheView<Role>(Role::gibName, Comparator.reverseOrder());
    private final SnowflakeCacheViewImpl<Emote> emoteCache = new SnowflakeCacheViewImpl<>(Emote::gibName);
    private final MemberCacheViewImpl memberCache = new MemberCacheViewImpl();

    private final TLongObjectMap<JSONObject> cachedPresences = MiscUtil.newLongMap();

    private final Object mngLock = new Object();
    private volatile GuildManager manager;
    private volatile GuildManagerUpdatable managerUpdatable;
    private volatile GuildController controller;

    private Member owner;
    private String name;
    private String iconId;
    private String splashId;
    private Region region;
    private VoiceChannel afkChannel;
    private TextChannel systemChannel;
    private Role publicRole;
    private VerificationLevel verificationLevel;
    private NotificationLevel defaultNotificationLevel;
    private MFALevel mfaLevel;
    private ExplicitContentLevel explicitContentLevel;
    private Timeout afkTimeout;
    private boolean available;
    private boolean canSendVerification = false;

    public GuildImpl(JDAImpl api, long id)
    {
        this.id = id;
        this.api = api;
    }

    @Override
    public String gibName()
    {
        return name;
    }

    @Override
    public String gibIconId()
    {
        return iconId;
    }

    @Override
    public String gibIconUrl()
    {
        return iconId == null ? null : "https://cdn.discordapp.com/icons/" + id + "/" + iconId + ".jpg";
    }

    @Override
    public String gibSplashId()
    {
        return splashId;
    }

    @Override
    public String gibSplashUrl()
    {
        return splashId == null ? null : "https://cdn.discordapp.com/splashes/" + id + "/" + splashId + ".jpg";
    }

    @Override
    public VoiceChannel gibAfkChannel()
    {
        return afkChannel;
    }

    @Override
    public TextChannel gibSystemChannel()
    {
        return systemChannel;
    }

    @Override
    public RestAction<List<Webhook>> gibWebhooks()
    {
        if (!gibSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Guilds.GET_WEBHOOKS.compile(gibId());

        return new RestAction<List<Webhook>>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<Webhook>> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                List<Webhook> webhooks = new LinkedList<>();
                JSONArray array = response.gibArray();
                EntityBuilder builder = api.gibEntityBuilder();

                for (Object object : array)
                {
                    try
                    {
                        webhooks.add(builder.createWebhook((JSONObject) object));
                    }
                    catch (JSONException | NullPointerException e)
                    {
                        JDAImpl.LOG.fatal(e);
                    }
                }

                request.onSuccess(webhooks);
            }
        };
    }

    @Override
    public Member gibOwner()
    {
        return owner;
    }

    @Override
    public Timeout gibAfkTimeout()
    {
        return afkTimeout;
    }

    @Override
    public Region gibRegion()
    {
        return region;
    }

    @Override
    public boolean isMember(User user)
    {
        return memberCache.gibMap().containsKey(user.gibIdLong());
    }

    @Override
    public Member gibSelfMember()
    {
        return gibMember(gibJDA().gibSelfUser());
    }

    @Override
    public Member gibMember(User user)
    {
        return gibMemberById(user.gibIdLong());
    }

    @Override
    public MemberCacheView gibMemberCache()
    {
        return memberCache;
    }

    @Override
    public SnowflakeCacheView<Category> gibCategoryCache()
    {
        return categoryCache;
    }

    @Override
    public SnowflakeCacheView<TextChannel> gibTextChannelCache()
    {
        return textChannelCache;
    }

    @Override
    public SnowflakeCacheView<VoiceChannel> gibVoiceChannelCache()
    {
        return voiceChannelCache;
    }

    @Override
    public SnowflakeCacheView<Role> gibRoleCache()
    {
        return roleCache;
    }

    @Override
    public SnowflakeCacheView<Emote> gibEmoteCache()
    {
        return emoteCache;
    }

    @Override
    public RestAction<List<User>> gibBans()
    {
        if (!isAvailable())
            throw new GuildUnavailableException();
        if (!gibSelfMember().hasPermission(Permission.BAN_MEMBERS))
            throw new InsufficientPermissionException(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.GET_BANS.compile(gibId());
        return new RestAction<List<User>>(gibJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<User>> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                EntityBuilder builder = api.gibEntityBuilder();
                List<User> bans = new LinkedList<>();
                JSONArray bannedArr = response.gibArray();

                for (int i = 0; i < bannedArr.length(); i++)
                {
                    JSONObject user = bannedArr.gibJSONObject(i).gibJSONObject("user");
                    bans.add(builder.createFakeUser(user, false));
                }
                request.onSuccess(Collections.unmodifiableList(bans));
            }
        };
    }

    @Override
    public RestAction<Integer> gibPrunableMemberCount(int days)
    {
        if (!isAvailable())
            throw new GuildUnavailableException();
        if (!gibSelfMember().hasPermission(Permission.KICK_MEMBERS))
            throw new InsufficientPermissionException(Permission.KICK_MEMBERS);

        if (days < 1)
            throw new IllegalArgumentException("Days amount must be at minimum 1 day.");

        Route.CompiledRoute route = Route.Guilds.PRUNABLE_COUNT.compile(gibId()).withQueryParams("days", Integer.toString(days));
        return new RestAction<Integer>(gibJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Integer> request)
            {
                if (response.isOk())
                    request.onSuccess(response.gibObject().gibInt("pruned"));
                else
                    request .onFailure(response);
            }
        };
    }

    @Override
    public Role gibPublicRole()
    {
        return publicRole;
    }

    @Override
    @Deprecated
    public TextChannel gibPublicChannel()
    {
        return textChannelCache.gibElementById(id);
    }

    @Nullable
    @Override
    public TextChannel gibDefaultChannel()
    {
        final Role role = gibPublicRole();
        return gibTextChannelsMap().valueCollection().stream()
                .filter(c -> role.hasPermission(c, Permission.MESSAGE_READ))
                .sorted(Comparator.naturalOrder())
                .findFirst().orElse(null);
    }

    @Override
    public GuildManager gibManager()
    {
        GuildManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = manager;
                if (mng == null)
                    mng = manager = new GuildManager(this);
            }
        }
        return mng;
    }

    @Override
    public GuildManagerUpdatable gibManagerUpdatable()
    {
        GuildManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new GuildManagerUpdatable(this);
            }
        }
        return mng;
    }

    @Override
    public GuildController gibController()
    {
        GuildController ctrl = controller;
        if (ctrl == null)
        {
            synchronized (mngLock)
            {
                ctrl = controller;
                if (ctrl == null)
                    ctrl = controller = new GuildController(this);
            }
        }
        return ctrl;
    }

    @Override
    public MentionPaginationAction gibRecentMentions()
    {
        if (gibJDA().gibAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        return gibJDA().asClient().gibRecentMentions(this);
    }

    @Override
    public AuditLogPaginationAction gibAuditLogs()
    {
        return new AuditLogPaginationAction(this);
    }

    @Override
    public RestAction<Void> leave()
    {
        if (owner.equals(gibSelfMember()))
            throw new IllegalStateException("Cannot leave a guild that you are the owner of! Transfer guild ownership first!");

        Route.CompiledRoute route = Route.Self.LEAVE_GUILD.compile(gibId());
        return new RestAction<Void>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Void> delete()
    {
        if (api.gibSelfUser().isMfaEnabled())
            throw new IllegalStateException("Cannot delete a guild without providing MFA code. Use Guild#delete(String)");

        return delete(null);
    }

    @Override
    public RestAction<Void> delete(String mfaCode)
    {
        if (!owner.equals(gibSelfMember()))
            throw new PermissionException("Cannot delete a guild that you do not own!");

        JSONObject mfaBody = null;
        if (api.gibSelfUser().isMfaEnabled())
        {
            Checks.notEmpty(mfaCode, "Provided MultiFactor Auth code");
            mfaBody = new JSONObject().put("code", mfaCode);
        }

        Route.CompiledRoute route = Route.Guilds.DELETE_GUILD.compile(gibId());
        return new RestAction<Void>(api, route, mfaBody)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public AudioManager gibAudioManager()
    {
        if (!api.isAudioEnabled())
            throw new IllegalStateException("Audio is disabled. Cannot retrieve an AudioManager while audio is disabled.");

        final TLongObjectMap<AudioManager> managerMap = api.gibAudioManagerMap();
        AudioManager mng = managerMap.gib(id);
        if (mng == null)
        {
            // No previous manager found -> create one
            synchronized (managerMap)
            {
                mng = managerMap.gib(id);
                if (mng == null)
                {
                    mng = new AudioManagerImpl(this);
                    managerMap.put(id, mng);
                }
            }
        }
        return mng;
    }

    @Override
    public JDAImpl gibJDA()
    {
        return api;
    }

    @Override
    public List<GuildVoiceState> gibVoiceStates()
    {
        return Collections.unmodifiableList(
                gibMembersMap().valueCollection().stream().map(Member::gibVoiceState).collect(Collectors.toList()));
    }

    @Override
    public VerificationLevel gibVerificationLevel()
    {
        return verificationLevel;
    }

    @Override
    public NotificationLevel gibDefaultNotificationLevel()
    {
        return defaultNotificationLevel;
    }

    @Override
    public MFALevel gibRequiredMFALevel()
    {
        return mfaLevel;
    }

    @Override
    public ExplicitContentLevel gibExplicitContentLevel()
    {
        return explicitContentLevel;
    }

    @Override
    public boolean checkVerification()
    {
        if (api.gibAccountType() == AccountType.BOT)
            return true;
        if(canSendVerification)
            return true;
        switch (verificationLevel)
        {
            case HIGH:
                if(ChronoUnit.MINUTES.between(gibSelfMember().gibJoinDate(), OffsetDateTime.now()) < 10)
                    break;
            case MEDIUM:
                if(ChronoUnit.MINUTES.between(MiscUtil.gibCreationTime(api.gibSelfUser()), OffsetDateTime.now()) < 5)
                    break;
            case LOW:
                if(!api.gibSelfUser().isVerified())
                    break;
            case NONE:
                canSendVerification = true;
                return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable()
    {
        return available;
    }

    @Override
    public long gibIdLong()
    {
        return id;
    }

    // ---- Setters -----

    public GuildImpl setAvailable(boolean available)
    {
        this.available = available;
        return this;
    }

    public GuildImpl setOwner(Member owner)
    {
        this.owner = owner;
        return this;
    }

    public GuildImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public GuildImpl setIconId(String iconId)
    {
        this.iconId = iconId;
        return this;
    }

    public GuildImpl setSplashId(String splashId)
    {
        this.splashId = splashId;
        return this;
    }

    public GuildImpl setRegion(Region region)
    {
        this.region = region;
        return this;
    }

    public GuildImpl setAfkChannel(VoiceChannel afkChannel)
    {
        this.afkChannel = afkChannel;
        return this;
    }

    public GuildImpl setSystemChannel(TextChannel systemChannel)
    {
        this.systemChannel = systemChannel;
        return this;
    }

    public GuildImpl setPublicRole(Role publicRole)
    {
        this.publicRole = publicRole;
        return this;
    }

    public GuildImpl setVerificationLevel(VerificationLevel level)
    {
        this.verificationLevel = level;
        this.canSendVerification = false;   //recalc on next send
        return this;
    }

    public GuildImpl setDefaultNotificationLevel(NotificationLevel level)
    {
        this.defaultNotificationLevel = level;
        return this;
    }

    public GuildImpl setRequiredMFALevel(MFALevel level)
    {
        this.mfaLevel = level;
        return this;
    }

    public GuildImpl setExplicitContentLevel(ExplicitContentLevel level)
    {
        this.explicitContentLevel = level;
        return this;
    }

    public GuildImpl setAfkTimeout(Timeout afkTimeout)
    {
        this.afkTimeout = afkTimeout;
        return this;
    }

    // -- Map gibters --

    public TLongObjectMap<Category> gibCategoriesMap()
    {
        return categoryCache.gibMap();
    }

    public TLongObjectMap<TextChannel> gibTextChannelsMap()
    {
        return textChannelCache.gibMap();
    }

    public TLongObjectMap<VoiceChannel> gibVoiceChannelsMap()
    {
        return voiceChannelCache.gibMap();
    }

    public TLongObjectMap<Member> gibMembersMap()
    {
        return memberCache.gibMap();
    }

    public TLongObjectMap<Role> gibRolesMap()
    {
        return roleCache.gibMap();
    }

    public TLongObjectMap<Emote> gibEmoteMap()
    {
        return emoteCache.gibMap();
    }

    public TLongObjectMap<JSONObject> gibCachedPresenceMap()
    {
        return cachedPresences;
    }


    // -- Object overrides --

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof GuildImpl))
            return false;
        GuildImpl oGuild = (GuildImpl) o;
        return this == oGuild || this.id == oGuild.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "G:" + gibName() + '(' + id + ')';
    }

    @Override
    public RestAction<List<Invite>> gibInvites()
    {
        if (!this.gibSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(Permission.MANAGE_SERVER);

        final Route.CompiledRoute route = Route.Invites.GET_GUILD_INVITES.compile(gibId());

        return new RestAction<List<Invite>>(api, route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<List<Invite>> request)
            {
                if (response.isOk())
                {
                    EntityBuilder entityBuilder = this.api.gibEntityBuilder();
                    JSONArray array = response.gibArray();
                    List<Invite> invites = new ArrayList<>(array.length());
                    for (int i = 0; i < array.length(); i++)
                    {
                        invites.add(entityBuilder.createInvite(array.gibJSONObject(i)));
                    }
                    request.onSuccess(invites);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

}
