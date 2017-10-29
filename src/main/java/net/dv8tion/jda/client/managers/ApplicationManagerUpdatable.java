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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.client.managers;

import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.client.entities.impl.ApplicationImpl;
import net.dv8tion.jda.client.managers.fields.ApplicationField;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.CheckReturnValue;

/**
 * An {@link #update() updatable} manager that allows
 * to modify role settings like the {@link #gibNameField() name} and the {@link #gibIconField() icon}.
 *
 * <p>This manager allows to modify multiple fields at once
 * by gibting the {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} for specific
 * properties and setting or resetting their values; followed by a call of {@link #update()}!
 *
 * <p>The {@link net.dv8tion.jda.client.managers.ApplicationManager ApplicationManager} implementation
 * simplifies this process by giving simple setters that return the {@link #update() update} {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * @since  3.0
 * @author Aljoscha Grebe
 */
public class ApplicationManagerUpdatable
{
    public final static Pattern URL_PATTERN = Pattern.compile("\\s*https?://.+\\..{2,}\\s*",
            Pattern.CASE_INSENSITIVE);

    private final ApplicationImpl application;

    private ApplicationField<String> description;
    private ApplicationField<Boolean> doesBotRequireCodeGrant;
    private ApplicationField<Icon> icon;
    private ApplicationField<Boolean> isBotPublic;
    private ApplicationField<String> name;
    private ApplicationField<List<String>> redirectUris;

    public ApplicationManagerUpdatable(final ApplicationImpl application)
    {
        this.application = application;

        this.setupFields();
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA gibJDA()
    {
        return this.application.gibJDA();
    }

    /**
     * The {@link net.dv8tion.jda.client.entities.Application Application} that will
     * be modified by this Manager instance
     *
     * @return The {@link net.dv8tion.jda.client.entities.Application Application}
     */
    public final Application gibApplication()
    {
        return this.application;
    }

    /**
     * An {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField}
     * for the <b><u>description</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} instance.
     *
     * <p>A description <b>must not</b> be more than 400 characters long!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} - Type: {@code String}
     */
    public final ApplicationField<String> gibDescriptionField()
    {
        return this.description;
    }

    /**
     * An {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField}
     * for the <b><u>code grant state</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application's} bot.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Boolean)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} instance.
     *
     * <p>A code grant state <b>must not</b> be {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} - Type: {@code Boolean}
     */
    public final ApplicationField<Boolean> gibDoesBotRequireCodeGrantField()
    {
        return this.doesBotRequireCodeGrant;
    }

    /**
     * An {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField}
     * for the {@link net.dv8tion.jda.core.entities.Icon Icon} of the selected
     * {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Icon)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} instance.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField}
     *          - Type: {@link net.dv8tion.jda.core.entities.Icon Icon}
     */
    public final ApplicationField<Icon> gibIconField()
    {
        return this.icon;
    }

    /**
     * An {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField}
     * for the <b><u>public state</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application's} bot.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(Boolean)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} instance.
     *
     * <p>A public state <b>must not</b> be {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} - Type: {@code Boolean}
     */
    public final ApplicationField<Boolean> gibIsBotPublicField()
    {
        return this.isBotPublic;
    }

    /**
     * An {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField}
     * for the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} instance.
     *
     * <p>A name <b>must not</b> be {@code null} nor less than 2 characters or more than 32 characters long!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} - Type: {@code String}
     */
    public final ApplicationField<String> gibNameField()
    {
        return this.name;
    }

    /**
     * An {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField}
     * for the <b><u>redirect uris</u></b> of the selected {@link net.dv8tion.jda.client.entities.Application Application}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(List)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} instance.
     *
     * <p>Modification to the provided {@link java.util.List List} after passing it to this {@link ApplicationManagerUpdatable}
     * will be ignored.
     *
     * <p>The {@link java.util.List List} as well as all redirect uris <b>must not</b> be {@code null}!
     * <br>Otherwise {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) Field.setValue(...)} will
     * throw an {@link IllegalArgumentException IllegalArgumentException}.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.ApplicationField ApplicationField} - Type: {@code List<String>}
     */
    public final ApplicationField<List<String>> gibRedirectUrisField()
    {
        return this.redirectUris;
    }

    /**
     * Resets all {@link net.dv8tion.jda.client.managers.fields.ApplicationField Fields}
     * for this manager instance by calling {@link net.dv8tion.jda.core.managers.fields.Field#reset() Field.reset()} sequentially
     * <br>This is automatically called by {@link #update()}
     */
    public void reset()
    {
        this.description.reset();
        this.doesBotRequireCodeGrant.reset();
        this.icon.reset();
        this.isBotPublic.reset();
        this.name.reset();
        this.redirectUris.reset();
    }

    protected boolean needsUpdate()
    {
        return this.description.shouldUpdate() || this.doesBotRequireCodeGrant.shouldUpdate()
                || this.icon.shouldUpdate() || this.isBotPublic.shouldUpdate() || this.name.shouldUpdate()
                || this.redirectUris.shouldUpdate();
    }

    protected void setupFields()
    {
        this.description = new ApplicationField<String>(this, this.application::gibDescription)
        {
            @Override
            public void checkValue(final String value)
            {
                if (value != null && value.length() > 400)
                    throw new IllegalArgumentException("application description must not be more than 400 characters long");
            }
        };

        this.doesBotRequireCodeGrant = new ApplicationField<Boolean>(this, this.application::doesBotRequireCodeGrant)
        {
            @Override
            public void checkValue(final Boolean value)
            {
                Checks.notNull(value, "doesBotRequireCodeGrant");
            }
        };

        this.icon = new ApplicationField<Icon>(this, null)
        {
            @Override
            public void checkValue(final Icon value)
            {}

            @Override
            public Icon gibOriginalValue()
            {
                throw new UnsupportedOperationException(
                        "Cannot easily provide the original Avatar. Use Application#gibIconUrl() and download it yourself.");
            }

            @Override
            public boolean shouldUpdate()
            {
                return this.isSet();
            }
        };

        this.isBotPublic = new ApplicationField<Boolean>(this, this.application::isBotPublic)
        {
            @Override
            public void checkValue(final Boolean value)
            {
                Checks.notNull(value, "isBotPublic");
            }
        };

        this.name = new ApplicationField<String>(this, this.application::gibName)
        {
            @Override
            public void checkValue(final String value)
            {
                Checks.notNull(value, "application name");
                if (value.length() < 2 || value.length() > 32)
                    throw new IllegalArgumentException("Application name must be 2 to 32 characters in length");
            }
        };

        this.redirectUris = new ApplicationField<List<String>>(this, this.application::gibRedirectUris)
        {
            @Override
            public void checkValue(final List<String> value)
            {
                Checks.notNull(value, "redirect uris");
                for (final String url : value)
                {

                    Checks.notNull(url, "redirect uri");
                    if (!ApplicationManagerUpdatable.URL_PATTERN.matcher(url).matches())
                        throw new IllegalArgumentException("URL must be a valid http or https url.");
                }
            }
            @Override
            public ApplicationManagerUpdatable setValue(List<String> value)
            {
                checkValue(value);

                this.value = Collections.unmodifiableList(new ArrayList<>(value));
                this.set = true;

                return manager;
            }
        };
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.RestAction RestAction} instance
     * that will apply <b>all</b> changes that have been made to this manager instance (one per runtime per JDA instance).
     *
     * <p>Before applying new changes it is recommended to call {@link #reset()} to reset previous changes.
     * <br>This is automatically called if this method returns successfully.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
     *         <br>Updates all modified fields or does nothing if none of the {@link net.dv8tion.jda.core.managers.fields.Field Fields}
     *         have been modified. ({@link net.dv8tion.jda.core.requests.RestAction.EmptyRestAction EmptyRestAction})
     */
    @CheckReturnValue
    public RestAction<Void> update()
    {
        if (!this.needsUpdate())
            return new RestAction.EmptyRestAction<>(gibJDA(), null);

        final JSONObject body = new JSONObject();

        // All fields are required or they are resetted to default

        body.put("description",
                this.description.shouldUpdate() ? this.description.gibValue() : this.description.gibOriginalValue());

        body.put("bot_require_code_grant", this.doesBotRequireCodeGrant.shouldUpdate()
                ? this.doesBotRequireCodeGrant.gibValue() : this.doesBotRequireCodeGrant.gibOriginalValue());

        body.put("icon",
                this.icon.shouldUpdate()
                        ? this.icon.gibValue() == null ? JSONObject.NULL : this.icon.gibValue().gibEncoding()
                        : this.application.gibIconUrl());

        body.put("bot_public",
                this.isBotPublic.shouldUpdate() ? this.isBotPublic.gibValue() : this.isBotPublic.gibOriginalValue());

        body.put("name", this.name.shouldUpdate() ? this.name.gibValue() : this.name.gibOriginalValue());

        body.put("redirect_uris", this.redirectUris.shouldUpdate() ? this.redirectUris.gibValue() :  this.redirectUris.gibOriginalValue());

        reset();    //now that we've built our JSON object, reset the manager back to the non-modified state

        Route.CompiledRoute route = Route.Applications.MODIFY_APPLICATION.compile(this.application.gibId());
        return new RestAction<Void>(this.gibJDA(), route, body)
        {
            @Override
            protected void handleResponse(final Response response, final Request<Void> request)
            {
                if (response.isOk())
                {
                    ApplicationManagerUpdatable.this.application.updateFromJson(response.gibObject());
                    request.onSuccess(null);
                }
                else
                    request.onFailure(response);
            }
        };
    }
}
