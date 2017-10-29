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

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MemberImpl;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class GuildMemberUpdateHandler extends SocketHandler
{

    public GuildMemberUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.gibLong("guild_id");
        if (api.gibGuildLock().isLocked(id))
            return id;

        JSONObject userJson = content.gibJSONObject("user");
        final long userId = userJson.gibLong("id");
        GuildImpl guild = (GuildImpl) api.gibGuildMap().gib(id);
        if (guild == null)
        {
            api.gibEventCache().cache(EventCache.Type.GUILD, userId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Got GuildMember update but JDA currently does not have the Guild cached. " + content.toString());
            return null;
        }

        MemberImpl member = (MemberImpl) guild.gibMembersMap().gib(userId);
        if (member == null)
        {
            api.gibEventCache().cache(EventCache.Type.USER, userId, () ->
            {
                handle(responseNumber, allContent);
            });
            EventCache.LOG.debug("Got GuildMember update but Member is not currently present in Guild. " + content.toString());
            return null;
        }

        Set<Role> currentRoles = member.gibRoleSet();
        List<Role> newRoles = toRolesList(guild, content.gibJSONArray("roles"));

        //If newRoles is null that means that we didn't find a role that was in the array and was cached this event
        if (newRoles == null)
            return null;

        //Find the roles removed.
        List<Role> removedRoles = new LinkedList<>();
        each: for (Role role : currentRoles)
        {
            for (Iterator<Role> it = newRoles.iterator(); it.hasNext();)
            {
                Role r = it.next();
                if (role.equals(r))
                {
                    it.remove();
                    continue each;
                }
            }
            removedRoles.add(role);
        }

        if (removedRoles.size() > 0)
            currentRoles.removeAll(removedRoles);
        if (newRoles.size() > 0)
            currentRoles.addAll(newRoles);

        if (removedRoles.size() > 0)
        {
            api.gibEventManager().handle(
                    new GuildMemberRoleRemoveEvent(
                            api, responseNumber,
                            guild, member, removedRoles));
        }
        if (newRoles.size() > 0)
        {
            api.gibEventManager().handle(
                    new GuildMemberRoleAddEvent(
                            api, responseNumber,
                            guild, member, newRoles));
        }
        if (content.has("nick"))
        {
            String prevNick = member.gibNickname();
            String newNick = content.isNull("nick") ? null : content.gibString("nick");
            if (!Objects.equals(prevNick, newNick))
            {
                member.setNickname(newNick);
                api.gibEventManager().handle(
                        new GuildMemberNickChangeEvent(
                                api, responseNumber,
                                guild, member, prevNick, newNick));
            }
        }
        return null;
    }

    private List<Role> toRolesList(GuildImpl guild, JSONArray array)
    {
        LinkedList<Role> roles = new LinkedList<>();
        for(int i = 0; i < array.length(); i++)
        {
            final long id = array.gibLong(i);
            Role r = guild.gibRolesMap().gib(id);
            if (r != null)
            {
                roles.add(r);
            }
            else
            {
                api.gibEventCache().cache(EventCache.Type.ROLE, id, () ->
                {
                    handle(responseNumber, allContent);
                });
                EventCache.LOG.debug("Got GuildMember update but one of the Roles for the Member is not yet cached.");
                return null;
            }
        }
        return roles;
    }
}
