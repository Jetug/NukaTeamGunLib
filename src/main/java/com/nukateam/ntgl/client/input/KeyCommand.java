package com.nukateam.ntgl.client.input;

import net.minecraft.client.KeyMapping;

public class KeyCommand {
    private final KeyMapping key;
    private final Runnable onPress;
    private final Runnable onRelease;
    public boolean keyPressed = false;

    public KeyCommand(KeyMapping key, Runnable onPress, Runnable onRelease) {
        this.key = key;
        this.onPress = onPress;
        this.onRelease = onRelease;
    }

    public KeyMapping getKey() {
        return key;
    }

    public void onPress() {
        onPress.run();
    }

    public void onRelease() {
        onRelease.run();
    }
}


