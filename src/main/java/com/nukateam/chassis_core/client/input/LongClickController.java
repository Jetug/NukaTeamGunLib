package com.nukateam.chassis_core.client.input;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class LongClickController {
    private final Map<Integer, Integer> keys = new HashMap<>();
    private Integer lastKey;
    private int ticks = 0;
    private BiConsumer<Integer, Integer> repeatListener = (k, t) -> {
    };
    private BiConsumer<Integer, Integer> releaseListener = (k, t) -> {
    };

    public LongClickController() {
        NeoForge.EVENT_BUS.addListener(this::onTick);
        NeoForge.EVENT_BUS.addListener(this::onMouseInput);
        NeoForge.EVENT_BUS.addListener(this::onKeyInput);
    }

    public void setRepeatListener(BiConsumer<Integer, Integer> listener) {
        this.repeatListener = listener;
    }

    public void setReleaseListener(BiConsumer<Integer, Integer> listener) {
        this.releaseListener = listener;
    }

    private void onTick(final ClientTickEvent.Pre event) {
//        if(lastKey == null) return;
//        ticks++;


        for (var key : keys.keySet()) {
            var ticks = keys.get(key) + 1;
            keys.put(key, ticks);

//            if (isNotInGame()) {
//                onRelease(key);
//                break;
//            } else repeatListener.accept(key, ticks);

            repeatListener.accept(key, ticks);
        }
    }

    private void onMouseInput(final InputEvent.MouseButton event) {
        onInput(event.getAction(), event.getButton());
    }

    private void onKeyInput(final InputEvent.Key event) {
        onInput(event.getAction(), event.getKey());
    }

    private void onInput(int action, int key) {
        switch (action) {
            case GLFW_PRESS -> onPress(key);
            case GLFW_RELEASE -> onRelease(key);
        }
    }

    private void onPress(int inputKey) {
        ticks = 0;
        //lastKey = inputKey;

        keys.put(inputKey, 0);
    }

    private void onRelease(int inputKey) {
        //if (lastKey == null || inputKey != lastKey) return;

        keys.remove(inputKey);

        //lastKey = null;
        releaseListener.accept(inputKey, ticks);
    }
}
