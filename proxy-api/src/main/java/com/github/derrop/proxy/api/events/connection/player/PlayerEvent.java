package com.github.derrop.proxy.api.events.connection.player;

import com.github.derrop.proxy.api.events.connection.ConnectionEvent;
import com.github.derrop.proxy.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerEvent extends ConnectionEvent {

    public PlayerEvent(@NotNull ProxiedPlayer player) {
        super(player);
        this.player = player;
    }

    private final ProxiedPlayer player;

    @Nullable
    public ProxiedPlayer getPlayer() {
        return player;
    }

}