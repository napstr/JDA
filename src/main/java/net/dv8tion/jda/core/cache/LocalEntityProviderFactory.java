package net.dv8tion.jda.core.cache;

//TODO docs all classes
public class LocalEntityProviderFactory implements EntityProviderFactory {

    @Override
    public <T> EntityProvider<T> createEntityProvider(String scope) {
        return new LocalEntityProvider<>();
    }
}
