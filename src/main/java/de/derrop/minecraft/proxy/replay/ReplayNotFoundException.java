package de.derrop.minecraft.proxy.replay;

import java.util.UUID;

public class ReplayNotFoundException extends RuntimeException {
    public ReplayNotFoundException(UUID replayId) {
        super("Replay " + replayId + " not found");
    }
}