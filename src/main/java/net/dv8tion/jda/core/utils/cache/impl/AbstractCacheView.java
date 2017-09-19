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

package net.dv8tion.jda.core.utils.cache.impl;

import net.dv8tion.jda.core.cache.EntityProvider;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.cache.CacheView;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCacheView<T> implements CacheView<T>
{
    protected final EntityProvider<T> entityProvider;
    protected final Function<T, String> nameMapper;

    protected AbstractCacheView(Function<T, String> nameMapper, EntityProvider<T> entityProvider)
    {
        this.nameMapper = nameMapper;
        this.entityProvider = entityProvider;
    }

    public void clear()
    {
        entityProvider.clear();
    }

    public EntityProvider<T> getMap()
    {
        return entityProvider;
    }//todo rename method

    @Override
    public List<T> asList()
    {
        return Collections.unmodifiableList(entityProvider.stream().collect(Collectors.toList()));
    }

    @Override
    public Set<T> asSet()
    {
        return Collections.unmodifiableSet(entityProvider.stream().collect(Collectors.toSet()));
    }

    @Override
    public long size()
    {
        return entityProvider.size();
    }

    @Override
    public boolean isEmpty()
    {
        return entityProvider.size() == 0;
    }

    @Override
    public List<T> getElementsByName(String name, boolean ignoreCase)
    {
        Checks.notEmpty(name, "Name");
        if (isEmpty())
            return Collections.emptyList();
        if (nameMapper == null) // no getName method available
            throw new UnsupportedOperationException("The contained elements are not assigned with names.");

        List<T> list = new LinkedList<>();
        entityProvider.stream().forEach(elem ->
        {
            String elementName = nameMapper.apply(elem);
            if (elementName != null)
            {
                if (ignoreCase)
                {
                    if (elementName.equalsIgnoreCase(name))
                        list.add(elem);
                }
                else
                {
                    if (elementName.equals(name))
                        list.add(elem);
                }
            }
        });
        return list;
    }

    @Override
    public Stream<T> stream()
    {
        return entityProvider.stream();
    }

    @Override
    public Stream<T> parallelStream()
    {
        return entityProvider.parallelStream();
    }

    @Nonnull
    @Override
    public Iterator<T> iterator()
    {
        return entityProvider.iterator();
    }
}
