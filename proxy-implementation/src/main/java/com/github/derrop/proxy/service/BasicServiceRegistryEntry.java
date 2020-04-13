package com.github.derrop.proxy.service;

import com.github.derrop.proxy.api.plugin.Plugin;
import com.github.derrop.proxy.api.service.ServiceRegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BasicServiceRegistryEntry<T> implements ServiceRegistryEntry<T> {

    BasicServiceRegistryEntry(Class<T> service, T provider, Plugin plugin, boolean immutable) {
        this.service = service;
        this.provider = provider;
        this.plugin = plugin;
        this.immutable = immutable;
    }

    private final Class<T> service;

    private final T provider;

    private final Plugin plugin;

    private final boolean immutable;

    @Override
    public @NotNull Class<T> getService() {
        return this.service;
    }

    @NotNull
    @Override
    public T getProvider() {
        return this.provider;
    }

    @Override
    public @Nullable Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isImmutable() {
        return this.immutable;
    }
}
