package com.nukateam.ntgl.modules.wheel;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ActionWheelManager {
    private static final ActionWheelManager INSTANCE = new ActionWheelManager();
    private final ActionWheel wheel;

    private ActionWheelManager() {
        this.wheel = new ActionWheel();
    }

    public static ActionWheelManager getInstance() {
        return INSTANCE;
    }

    public void hideWheel() {
        if (wheel.isVisible()) {
            wheel.hide();
        }
    }

    public void showWheel(List<ActionWheel.WheelAction> actions, Component title, Runnable defaultAction) {
        wheel.show(actions, title, defaultAction);
    }

    public ActionWheel getWheel() {
        return wheel;
    }

    public boolean isWheelActive() {
        return wheel.isVisible();
    }
}