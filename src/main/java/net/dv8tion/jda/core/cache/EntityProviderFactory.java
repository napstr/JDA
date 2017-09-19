package net.dv8tion.jda.core.cache;

//TODO docs this class
//TODO copyright for the whole PR

public interface EntityProviderFactory {

    //todo add a scope
    <T> EntityProvider<T> createEntityProvider(Class<T> clazz);
}
