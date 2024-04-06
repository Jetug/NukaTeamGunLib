package com.nukateam.ntgl.common.debug.screen.widget;

import com.nukateam.ntgl.common.debug.IDebugWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class DebugToggle extends DebugButton implements IDebugWidget {
    private final Consumer<Boolean> callback;
    private boolean enabled;

    public DebugToggle(boolean initialValue, Consumer<Boolean> callback) {
        super(Component.empty(), btn -> ((DebugToggle) btn).toggle());
        this.enabled = initialValue;
        this.callback = callback;
        this.updateMessage();
    }

    private void toggle() {
        this.enabled = !this.enabled;
        this.updateMessage();
        this.callback.accept(this.enabled);
    }

    private void updateMessage() {
        this.setMessage(this.enabled ? Component.literal("On") : Component.literal("Off"));
    }
}
