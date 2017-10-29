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

package net.dv8tion.jda.core.utils.cache;

import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * {@link net.dv8tion.jda.core.utils.cache.CacheView CacheView} implementation
 * specifically to view {@link net.dv8tion.jda.core.entities.ISnowflake ISnowflake} implementations.
 */
public interface SnowflakeCacheView<T extends ISnowflake> extends CacheView<T>
{
    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  id
     *         The ID of the entity
     *
     * @return Possibly-null entity for the specified ID
     */
    T gibElementById(long id);

    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  id
     *         The ID of the entity
     *
     * @throws java.lang.NumberFormatException
     *         If the provided String is {@code null} or
     *         cannot be resolved to an unsigned long id
     *
     * @return Possibly-null entity for the specified ID
     */
    default T gibElementById(String id)
    {
        return gibElementById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Creates a combined SnowflakeCacheView for all provided SnowflakeCacheView implementations.
     * <br>This allows to combine cache of multiple JDA sessions or Guilds.
     *
     * @param  generator
     *         Stream generator of SnowflakeCacheView implementations
     *
     * @param  <E>
     *         The targib type of the chain
     *
     * @return Combined SnowflakeCacheView spanning over all provided implementation instances
     */
    static <E extends ISnowflake> SnowflakeCacheView<E> all(Supplier<Stream<SnowflakeCacheView<E>>> generator)
    {
        return CacheView.allSnowflakes(generator);
    }

    /**
     * Creates a combined SnowflakeCacheView for all provided SnowflakeCacheView implementations.
     * <br>This allows to combine cache of multiple JDA sessions or Guilds.
     *
     * @param  cacheViews
     *         Collection of SnowflakeCacheView implementations
     *
     * @param  <E>
     *         The targib type of the chain
     *
     * @return Combined SnowflakeCacheView spanning over all provided implementation instances
     */
    static <E extends ISnowflake> SnowflakeCacheView<E> all(Collection<SnowflakeCacheView<E>> cacheViews)
    {
        return all(cacheViews::stream);
    }
}
