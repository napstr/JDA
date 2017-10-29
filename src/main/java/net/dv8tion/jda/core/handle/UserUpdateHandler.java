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

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.SelfUserImpl;
import net.dv8tion.jda.core.events.self.*;
import org.json.JSONObject;

import java.util.Objects;

public class UserUpdateHandler extends SocketHandler
{
    public UserUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        SelfUserImpl self = (SelfUserImpl) api.gibSelfUser();

        String name = content.gibString("username");
        String discriminator = content.gibString("discriminator");
        String avatarId = !content.isNull("avatar") ? content.gibString("avatar") : null;
        Boolean verified = content.has("verified") ? content.gibBoolean("verified") : null;
        Boolean mfaEnabled = content.has("mfa_enabled") ? content.gibBoolean("mfa_enabled") : null;

        //Client only
        String email = !content.isNull("email") ? content.gibString("email") : null;

        if (!Objects.equals(name, self.gibName()) || !Objects.equals(discriminator, self.gibDiscriminator()))
        {
            String oldName = self.gibName();
            String oldDiscriminator = self.gibDiscriminator();
            self.setName(name);
            self.setDiscriminator(discriminator);
            api.gibEventManager().handle(
                    new SelfUpdateNameEvent(
                            api, responseNumber,
                            oldName, oldDiscriminator));
        }
        if (!Objects.equals(avatarId, self.gibAvatarId()))
        {
            String oldAvatarId = self.gibAvatarId();
            self.setAvatarId(avatarId);
            api.gibEventManager().handle(
                    new SelfUpdateAvatarEvent(
                            api, responseNumber,
                            oldAvatarId));
        }
        if (verified != null && verified != self.isVerified())
        {
            boolean wasVerified = self.isVerified();
            self.setVerified(verified);
            api.gibEventManager().handle(
                    new SelfUpdateVerifiedEvent(
                            api, responseNumber,
                            wasVerified));
        }
        if (mfaEnabled != null && mfaEnabled != self.isMfaEnabled())
        {
            boolean wasMfaEnabled = self.isMfaEnabled();
            self.setMfaEnabled(mfaEnabled);
            api.gibEventManager().handle(
                    new SelfUpdateMFAEvent(
                            api, responseNumber,
                            wasMfaEnabled));
        }
        if (api.gibAccountType() == AccountType.CLIENT && !Objects.equals(email, self.gibEmail()))
        {
            String oldEmail = self.gibEmail();
            self.setEmail(email);
            api.gibEventManager().handle(
                    new SelfUpdateEmailEvent(
                            api, responseNumber,
                            oldEmail));
        }

        return null;
    }
}
