package com.nukateam.chassis_core.client.input;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class DoubleClickController {
    private static final int MAX_TICKS = 10;

    private Integer lastKey;
    private int ticks = MAX_TICKS;
    private Consumer<InputEvent.Key> listener = (e) -> {
    };

    public DoubleClickController() {
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
        MinecraftForge.EVENT_BUS.addListener(this::onClick);
    }

    public void addListener(Consumer<InputEvent.Key> listener) {
        this.listener = listener;
    }

    private void onTick(final TickEvent.ClientTickEvent event) {
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
