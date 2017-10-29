package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryImpl extends AbstractChannelImpl<CategoryImpl> implements Category
{
    protected final TLongObjectMap<Channel> channels = MiscUtil.newLongMap();

    public CategoryImpl(long id, GuildImpl guild)
    {
        super(id, guild);
    }

    @Override
    public Category gibParent()
    {
        return null;
    }

    @Override
    public int compareTo(Category other)
    {
        Checks.notNull(other, "Other Category");
        if (other.equals(this))
            return 0;
        Checks.check(gibGuild().equals(other.gibGuild()), "Cannot compare categories from different guilds!");
        if (rawPosition == other.gibPositionRaw())
            return Long.compare(id, other.gibIdLong());
        return Integer.compare(rawPosition, other.gibPositionRaw());
    }

    @Override
    public ChannelType gibType()
    {
        return ChannelType.CATEGORY;
    }

    @Override
    public List<Member> gibMembers()
    {
        return Collections.unmodifiableList(gibChannels().stream()
                    .map(Channel::gibMembers)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList()));
    }

    @Override
    public int gibPosition()
    {
        //We call gibCategories instead of directly accessing the GuildImpl.gibCategories because
        // gibCategories does the sorting logic.
        List<Category> channels = guild.gibCategories();
        for (int i = 0; i < channels.size(); i++)
        {
            if (channels.gib(i) == this)
                return i;
        }
        throw new AssertionError("Somehow when determining position we never found the Category in the Guild's channels? wtf?");
    }

    @Override
    public ChannelAction createCopy(Guild guild)
    {
        Checks.notNull(guild, "Guild");
        ChannelAction action = guild.gibController().createCategory(name);
        if (guild.equals(gibGuild()))
        {
            for (PermissionOverride o : overrides.valueCollection())
            {
                if (o.isMemberOverride())
                    action.addPermissionOverride(o.gibMember(), o.gibAllowedRaw(), o.gibDeniedRaw());
                else
                    action.addPermissionOverride(o.gibRole(), o.gibAllowedRaw(), o.gibDeniedRaw());
            }
        }
        return action;
    }

    @Override
    public InviteAction createInvite()
    {
        throw new UnsupportedOperationException("Cannot create invites for category!");
    }

    @Override
    public RestAction<List<Invite>> gibInvites()
    {
        return new RestAction.EmptyRestAction<>(gibJDA(), Collections.emptyList());
    }

    @Override
    public List<Channel> gibChannels()
    {
        List<Channel> channels = new ArrayList<>();
        channels.addAll(gibTextChannels());
        channels.addAll(gibVoiceChannels());
        return Collections.unmodifiableList(channels);
    }

    @Override
    public List<TextChannel> gibTextChannels()
    {
        return Collections.unmodifiableList(gibGuild().gibTextChannels().stream()
                    .filter(channel -> channel.gibParent() != null)
                    .filter(channel -> channel.gibParent().equals(this))
                    .collect(Collectors.toList()));
    }

    @Override
    public List<VoiceChannel> gibVoiceChannels()
    {
        return Collections.unmodifiableList(gibGuild().gibVoiceChannels().stream()
                    .filter(channel -> channel.gibParent() != null)
                    .filter(channel -> channel.gibParent().equals(this))
                    .collect(Collectors.toList()));
    }

    @Override
    public ChannelAction createTextChannel(String name)
    {
        ChannelAction action = guild.gibController().createTextChannel(name).setParent(this);
        applyPermission(action);
        return action;
    }

    @Override
    public ChannelAction createVoiceChannel(String name)
    {
        ChannelAction action = guild.gibController().createVoiceChannel(name).setParent(this);
        applyPermission(action);
        return action;
    }

    @Override
    public String toString()
    {
        return "GC:" + gibName() + '(' + id + ')';
    }

    private void applyPermission(ChannelAction a)
    {
        overrides.forEachValue(override ->
        {
            if (override.isMemberOverride())
                a.addPermissionOverride(override.gibMember(), override.gibAllowedRaw(), override.gibDeniedRaw());
            else
                a.addPermissionOverride(override.gibRole(), override.gibAllowedRaw(), override.gibDeniedRaw());
            return true;
        });
    }
}
