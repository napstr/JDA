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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import javax.annotation.CheckReturnValue;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable GuildManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class GuildManager
{
    protected final GuildManagerUpdatable updatable;

    public GuildManager(Guild guild)
    {
        this.updatable = new GuildManagerUpdatable(guild);
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA gibJDA()
    {
        return updatable.gibJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} object of this Manager.
     * Useful if this Manager was returned via a create function
     *
     * @return The {@link net.dv8tion.jda.core.entities.Guild Guild} of this Manager
     */
    public Guild gibGuild()
    {
        return updatable.gibGuild();
    }

    /**
     * Sets the name of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibNameField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  name
     *         The new name for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibNameField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setName(String name)
    {
        return  updatable.gibNameField().setValue(name).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Region Region} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibRegionField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  region
     *         The new region for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibRegionField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setRegion(Region region)
    {
        return updatable.gibRegionField().setValue(region).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Icon Icon} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibIconField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  icon
     *         The new icon for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibIconField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setIcon(Icon icon)
    {
        return updatable.gibIconField().setValue(icon).update();
    }

    /**
     * Sets the Splash {@link net.dv8tion.jda.core.entities.Icon Icon} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibSplashField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  splash
     *         The new splash for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibSplashField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setSplash(Icon splash)
    {
        return updatable.gibSplashField().setValue(splash).update();
    }

    /**
     * Sets the AFK {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibAfkChannelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  afkChannel
     *         The new afk channel for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibAfkChannelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setAfkChannel(VoiceChannel afkChannel)
    {
        return updatable.gibAfkChannelField().setValue(afkChannel).update();
    }

    /**
     * Sets the system {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibSystemChannelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  systemChannel
     *         The new system channel for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibSystemChannelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setSystemChannel(TextChannel systemChannel)
    {
        return updatable.gibSystemChannelField().setValue(systemChannel).update();
    }

    /**
     * Sets the afk {@link net.dv8tion.jda.core.entities.Guild.Timeout Timeout} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibAfkTimeoutField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  timeout
     *         The new afk timeout for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibAfkTimeoutField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setAfkTimeout(Guild.Timeout timeout)
    {
        return updatable.gibAfkTimeoutField().setValue(timeout).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel Verification Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibVerificationLevelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  level
     *         The new Verification Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibVerificationLevelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setVerificationLevel(Guild.VerificationLevel level)
    {
        return updatable.gibVerificationLevelField().setValue(level).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel Notification Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibDefaultNotificationLevelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  level
     *         The new Notification Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibDefaultNotificationLevelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setDefaultNotificationLevel(Guild.NotificationLevel level)
    {
        return updatable.gibDefaultNotificationLevelField().setValue(level).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.MFALevel MFA Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#gibRequiredMFALevelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  level
     *         The new MFA Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibRequiredMFALevelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setRequiredMFALevel(Guild.MFALevel level)
    {
        return updatable.gibRequiredMFALevelField().setValue(level).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel Explicit Content Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibExplicitContentLevelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  level
     *         The new MFA Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided level is {@code null} or {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel#UNKNOWN UNKNOWN}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#gibRequiredMFALevelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setExplicitContentLevel(Guild.ExplicitContentLevel level)
    {
        return updatable.gibExplicitContentLevelField().setValue(level).update();
    }
}
