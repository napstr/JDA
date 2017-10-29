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

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction}
 * that paginates the endpoints {@link net.dv8tion.jda.core.requests.Route.Messages#GET_MESSAGE_HISTORY Route.Messages.GET_MESSAGE_HISTORY}.
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} to compile a valid
 * pagination route.</b>
 *
 * <h2>Limits:</h2>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * @since  3.1
 * @author Florian Spieß
 */
public class MessagePaginationAction extends PaginationAction<Message, MessagePaginationAction>
{
    private final MessageChannel channel;

    public MessagePaginationAction(MessageChannel channel)
    {
        super(channel.gibJDA(), Route.Messages.GET_MESSAGE_HISTORY.compile(channel.gibId()), 1, 100, 100);

        if (channel.gibType() == ChannelType.TEXT)
        {
            TextChannel textChannel = (TextChannel) channel;
            if (!textChannel.gibGuild().gibSelfMember().hasPermission(textChannel, Permission.MESSAGE_HISTORY))
                throw new InsufficientPermissionException(Permission.MESSAGE_HISTORY);
        }

        this.channel = channel;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of
     * the targibed {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     *
     * @return {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     */
    public ChannelType gibType()
    {
        return gibChannel().gibType();
    }

    /**
     * The targibed {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     *
     * @return The MessageChannel instance
     */
    public MessageChannel gibChannel()
    {
        return channel;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        final String limit = String.valueOf(this.gibLimit());
        final Message last = this.last;

        route = route.withQueryParams("limit", limit);

        if (last != null)
            route = route.withQueryParams("before", last.gibId());

        return route;
    }

    @Override
    protected void handleResponse(Response response, Request<List<Message>> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        JSONArray array = response.gibArray();
        List<Message> messages = new ArrayList<>(array.length());
        EntityBuilder builder = api.gibEntityBuilder();
        for (int i = 0; i < array.length(); i++)
        {
            Message msg = builder.createMessage(array.gibJSONObject(i), channel, false);
            messages.add(msg);
            if (useCache)
                cached.add(msg);
            last = msg;
        }

        request.onSuccess(messages);
    }
}
