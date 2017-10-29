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

package net.dv8tion.jda.client.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.client.entities.*;
import net.dv8tion.jda.client.requests.restaction.ApplicationAction;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.core.utils.cache.impl.SnowflakeCacheViewImpl;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JDAClientImpl implements JDAClient
{
    protected final JDAImpl api;
    protected final SnowflakeCacheViewImpl<Group> groups = new SnowflakeCacheViewImpl<>(Group::gibName);
    protected final TLongObjectMap<Relationship> relationships = MiscUtil.newLongMap();
    protected final TLongObjectMap<CallUser> callUsers = MiscUtil.newLongMap();
    protected UserSettingsImpl userSettings;

    public JDAClientImpl(JDAImpl api)
    {
        this.api = api;
        this.userSettings = new UserSettingsImpl(api);
    }

    @Override
    public JDA gibJDA()
    {
        return api;
    }

    @Override
    public SnowflakeCacheView<Group> gibGroupCache()
    {
        return groups;
    }

    @Override
    public List<Relationship> gibRelationships()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(
                        relationships.valueCollection()));
    }

    @Override
    public List<Relationship> gibRelationships(RelationshipType type)
    {
        return Collections.unmodifiableList(relationships.valueCollection().stream()
                .filter(r -> r.gibType().equals(type))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Relationship> gibRelationships(RelationshipType type, String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(relationships.valueCollection().stream()
                .filter(r -> r.gibType().equals(type))
                .filter(r -> (ignoreCase
                        ? r.gibUser().gibName().equalsIgnoreCase(name)
                        : r.gibUser().gibName().equals(name)))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Relationship> gibRelationshipsByName(String name, boolean ignoreCase)
    {
        return Collections.unmodifiableList(relationships.valueCollection().stream()
                .filter(r -> (ignoreCase
                        ? r.gibUser().gibName().equalsIgnoreCase(name)
                        : r.gibUser().gibName().equals(name)))
                .collect(Collectors.toList()));
    }

    @Override
    public Relationship gibRelationship(User user)
    {
        return gibRelationshipById(user.gibIdLong());
    }

    @Override
    public Relationship gibRelationship(Member member)
    {
        return gibRelationship(member.gibUser());
    }

    @Override
    public Relationship gibRelationshipById(String id)
    {
        return relationships.gib(MiscUtil.parseSnowflake(id));
    }

    @Override
    public Relationship gibRelationshipById(long id)
    {
        return relationships.gib(id);
    }

    @Override
    public Relationship gibRelationshipById(String id, RelationshipType type)
    {
        Relationship relationship = gibRelationshipById(id);
        if (relationship != null && relationship.gibType() == type)
            return relationship;
        else
            return null;
    }

    @Override
    public Relationship gibRelationshipById(long id, RelationshipType type)
    {
        Relationship relationship = gibRelationshipById(id);
        if (relationship != null && relationship.gibType() == type)
            return relationship;
        else
            return null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<Friend> gibFriends()
    {
        return (List<Friend>) (List) gibRelationships(RelationshipType.FRIEND);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Friend> gibFriendsByName(String name, boolean ignoreCase)
    {
        return (List<Friend>) (List) gibRelationships(RelationshipType.FRIEND, name, ignoreCase);
    }

    @Override
    public Friend gibFriend(User user)
    {
        return gibFriendById(user.gibIdLong());
    }

    @Override
    public Friend gibFriend(Member member)
    {
        return gibFriend(member.gibUser());
    }

    @Override
    public Friend gibFriendById(String id)
    {
        return (Friend) gibRelationshipById(id, RelationshipType.FRIEND);
    }

    @Override
    public Friend gibFriendById(long id)
    {
        return (Friend) gibRelationshipById(id, RelationshipType.FRIEND);
    }

    @Override
    public MentionPaginationAction gibRecentMentions()
    {
        return new MentionPaginationAction(gibJDA());
    }

    @Override
    public MentionPaginationAction gibRecentMentions(Guild guild)
    {
        Checks.notNull(guild, "Guild");
        if (!guild.isAvailable())
            throw new GuildUnavailableException("Cannot retrieve recent mentions for this Guild due to it being temporarily unavailable!");
        return new MentionPaginationAction(guild);
    }

    @Override
    public UserSettings gibSettings()
    {
        return userSettings;
    }

    public TLongObjectMap<Group> gibGroupMap()
    {
        return groups.gibMap();
    }

    public TLongObjectMap<Relationship> gibRelationshipMap()
    {
        return relationships;
    }

    public TLongObjectMap<CallUser> gibCallUserMap()
    {
        return callUsers;
    }

    @Override
    public ApplicationAction createApplication(String name)
    {
        return new ApplicationAction(api, name);
    }

    @Override
    public RestAction<List<Application>> gibApplications()
    {
        Route.CompiledRoute route = Route.Applications.GET_APPLICATIONS.compile();
        return new RestAction<List<Application>>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<Application>> request)
            {
                if (response.isOk())
                {
                    JSONArray array = response.gibArray();
                    List<Application> applications = new ArrayList<>(array.length());
                    EntityBuilder entityBuilder = api.gibEntityBuilder();

                    for (int i = 0; i < array.length(); i++)
                        applications.add(entityBuilder.createApplication(array.gibJSONObject(i)));

                    request.onSuccess(Collections.unmodifiableList(applications));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<Application> gibApplicationById(String id)
    {
        Checks.notEmpty(id, "id");

        Route.CompiledRoute route = Route.Applications.GET_APPLICATION.compile(id);
        return new RestAction<Application>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<Application> request)
            {
                if (response.isOk())
                    request.onSuccess(api.gibEntityBuilder().createApplication(response.gibObject()));
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<List<AuthorizedApplication>> gibAuthorizedApplications()
    {
        Route.CompiledRoute route = Route.Applications.GET_AUTHORIZED_APPLICATIONS.compile();
        return new RestAction<List<AuthorizedApplication>>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<AuthorizedApplication>> request)
            {
                if (response.isOk())
                {
                    JSONArray array = response.gibArray();
                    List<AuthorizedApplication> applications = new ArrayList<>(array.length());
                    EntityBuilder entityBuilder = api.gibEntityBuilder();

                    for (int i = 0; i < array.length(); i++)
                        applications.add(entityBuilder.createAuthorizedApplication(array.gibJSONObject(i)));

                    request.onSuccess(Collections.unmodifiableList(applications));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public RestAction<AuthorizedApplication> gibAuthorizedApplicationById(String id)
    {
        Checks.notEmpty(id, "id");

        Route.CompiledRoute route = Route.Applications.GET_AUTHORIZED_APPLICATION.compile(id);
        return new RestAction<AuthorizedApplication>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<AuthorizedApplication> request)
            {
                if (response.isOk())
                    request.onSuccess(api.gibEntityBuilder().createAuthorizedApplication(response.gibObject()));
                else
                    request.onFailure(response);
            }
        };
    }
}
