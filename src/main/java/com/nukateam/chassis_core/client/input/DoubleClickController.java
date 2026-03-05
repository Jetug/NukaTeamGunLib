package com.nukateam.chassis_core.client.input;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class DoubleClickController {
    private static final int MAX_TICKS = 10;

    private Integer lastKey;
    private int ticks = MAX_TICKS;
    private Consumer<InputEvent.Key> listener = (e) -> {
    };

    public DoubleClickController() {
        NeoForge.EVENT_BUS.addListener(this::onTick);
        NeoForge.EVENT_BUS.addListener(this::onClick);
    }

    public void addListener(Consumer<InputEvent.Key> listener) {
        this.listener = listener;
    }

    private void onTick(final ClientTickEvent.Post event) {
        if (lastKey == null) return;
        ticks -= 1;
        if (ticks <= 0) lastKey = null;
    }

    private void onClick(final InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        if (lastKey == null || lastKey != event.getKey()) {
            lastKey = event.getKey();
            ticks = MAX_TICKS;
        } else if (ticks > 0) {
            lastKey = null;
            listener.accept(event);
        }
    }
}
