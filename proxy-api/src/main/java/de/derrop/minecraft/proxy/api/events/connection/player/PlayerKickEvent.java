package de.derrop.minecraft.proxy.api.events.connection.player;

import de.derrop.minecraft.proxy.api.chat.component.BaseComponent;
import de.derrop.minecraft.proxy.api.connection.ProxiedPlayer;
import de.derrop.minecraft.proxy.api.event.Cancelable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerKickEvent extends PlayerEvent implements Cancelable { // TODO

    private boolean cancel;
    private BaseComponent[] reason;

    public PlayerKickEvent(@NotNull ProxiedPlayer player, @Nullable BaseComponent[] reason) {
        super(player);
        this.reason = reason;
    }

    @Override
    public void cancel(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }
}
