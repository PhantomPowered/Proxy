package com.github.derrop.proxy.api.events.connection.service.scoreboard;

import com.github.derrop.proxy.api.connection.ServiceConnection;
import com.github.derrop.proxy.api.scoreboard.Objective;
import org.jetbrains.annotations.NotNull;

public class ScoreboardObjectiveRegisterEvent extends ScoreboardEvent {

    private final Objective objective;

    public ScoreboardObjectiveRegisterEvent(@NotNull ServiceConnection connection, Objective objective) {
        super(connection);
        this.objective = objective;
    }

    public Objective getObjective() {
        return this.objective;
    }
}
