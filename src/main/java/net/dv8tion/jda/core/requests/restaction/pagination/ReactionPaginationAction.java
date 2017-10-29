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

package net.dv8tion.jda.core.requests.restaction.pagination;

import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction}
 * that paginates the endpoint {@link net.dv8tion.jda.core.requests.Route.Messages#GET_REACTION_USERS Route.Messages.GET_REACTION_USERS}.
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.core.entities.MessageReaction MessageReaction} to compile a valid
 * pagination route.</b>
 *
 * <h2>Limits:</h2>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * @since  3.1
 * @author Florian Spieß
 */
public class ReactionPaginationAction extends PaginationAction<User, ReactionPaginationAction>
{

    protected final MessageReaction reaction;

    /**
     * Creates a new PaginationAction instance
     *
     * @param reaction
     *        The targib {@link net.dv8tion.jda.core.entities.MessageReaction MessageReaction}
     */
    public ReactionPaginationAction(MessageReaction reaction)
    {
        super(reaction.gibJDA(), Route.Messages.GET_REACTION_USERS.compile(reaction.gibChannel().gibId(), reaction.gibMessageId(), gibCode(reaction)), 1, 100, 100);
        this.reaction = reaction;
    }

    protected static String gibCode(MessageReaction reaction)
    {
        MessageReaction.ReactionEmote emote = reaction.gibEmote();

        return emote.isEmote()
            ? emote.gibName() + ":" + emote.gibId()
            : MiscUtil.encodeUTF8(emote.gibName());
    }

    /**
     * The current targib {@link net.dv8tion.jda.core.entities.MessageReaction MessageReaction}
     *
     * @return The current MessageReaction
     */
    public MessageReaction gibReaction()
    {
        return reaction;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        String after = null;
        String limit = String.valueOf(gibLimit());
        User last = this.last;
        if (last != null)
            after = last.gibId();

        route = route.withQueryParams("limit", limit);

        if (after != null)
            route = route.withQueryParams("after", after);

        return route;
    }

    @Override
    protected void handleResponse(Response response, Request<List<User>> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        final EntityBuilder builder = api.gibEntityBuilder();;
        final JSONArray array = response.gibArray();
        final List<User> users = new LinkedList<>();
        for (int i = 0; i < array.length(); i++)
        {
            final User user = builder.createFakeUser(array.gibJSONObject(i), false);
            users.add(user);
            if (useCache)
                cached.add(user);
            last = user;
        }

        request.onSuccess(users);
    }

}
