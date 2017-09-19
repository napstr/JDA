package net.dv8tion.jda.core.cache;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.Iterator;
import java.util.stream.Stream;

//TODO docs
public class LocalEntityProvider<T> implements EntityProvider<T> {

    private TLongObjectMap<T> elements = MiscUtil.newLongMap();

    @Override
    public T get(long key) {
        return elements.get(key);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public Stream<T> stream() {
        return elements.valueCollection().stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return elements.valueCollection().parallelStream();
    }

    @Override
    public Iterator<T> iterator() {
        return elements.valueCollection().iterator();
    }

    @Override
    public boolean hasEntity(long key) {
        return elements.containsKey(key);
    }

    @Override
    public T put(long key, T entity) {
        return elements.put(key, entity);
    }

    @Override
    public T remove(long key) {
        return elements.remove(key);
    }

    @Override
    public long size() {
        return elements.size();
    }
}
