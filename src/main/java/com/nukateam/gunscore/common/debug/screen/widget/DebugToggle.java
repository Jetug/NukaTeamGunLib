package com.nukateam.gunscore.common.debug.screen.widget;

import com.nukateam.gunscore.common.debug.IDebugWidget;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class DebugToggle extends DebugButton implements IDebugWidget {
    private boolean enabled;
    private final Consumer<Boolean> callback;

    public DebugToggle(boolean initialValue, Consumer<Boolean> callback) {
        super(TextComponent.EMPTY, btn -> ((DebugToggle) btn).toggle());
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
        this.setMessage(this.enabled ? new TextComponent("On") : new TextComponent("Off"));
    }
}
