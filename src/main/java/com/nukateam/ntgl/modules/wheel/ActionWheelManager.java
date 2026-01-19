package com.nukateam.ntgl.modules.wheel;

import net.minecraft.client.Minecraft;

import java.util.List;

public class ActionWheelManager {
    private static final ActionWheelManager INSTANCE = new ActionWheelManager();
    private final ActionWheel wheel;

    private ActionWheelManager() {
        this.wheel = new ActionWheel(Minecraft.getInstance());
    }

    public static ActionWheelManager getInstance() {
        return INSTANCE;
    }

    public void hideWheel() {
        if (wheel.isVisible()) {
            wheel.hide();
        }
    }

    public void showWheel(List<ActionWheel.WheelAction> actions) {
        wheel.show(actions);
    }

    public ActionWheel getWheel() {
        return wheel;
    }

    public boolean isWheelActive() {
        return wheel.isVisible();
    }
}