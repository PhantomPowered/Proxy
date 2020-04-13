package de.derrop.minecraft.proxy.api.task;

import org.jetbrains.annotations.NotNull;

public interface TaskFutureListener<V> {

    default void onCancel(@NotNull Task<V> task) {
    }

    default void onFailure(@NotNull Task<V> task) {
    }

    default void onSuccess(@NotNull Task<V> task) {
    }
}