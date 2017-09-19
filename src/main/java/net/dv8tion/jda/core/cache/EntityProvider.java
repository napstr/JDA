package net.dv8tion.jda.core.cache;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Created by napster on 19.09.17.
 *
 * Provides access to cached entites
 */
public interface EntityProvider<E> {

    //possible weird Es found so far:
    //JSONObject


    E get(long id);

    void clear();

    Stream<E> stream();

    Stream<E> parallelStream();

    Iterator<E> iterator();

    boolean hasEntity(long key);

    E put(long key, E entity);

    E remove(long key);

    long size();
}
