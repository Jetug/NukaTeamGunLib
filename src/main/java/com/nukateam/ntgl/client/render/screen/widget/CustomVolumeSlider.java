package com.nukateam.ntgl.client.render.screen.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class CustomVolumeSlider extends AbstractSliderButton {
    public CustomVolumeSlider(int x, int y, int width, int height, Component message, double value) {
        super(x, y, width, height, message, value);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal((int) (this.value * 100) + "%"));
    }

    @Override
    protected void applyValue() {
//        // Сохраняем значение при изменении слайдера
//        ModConfig.CUSTOM_VOLUME.set(this.value * 100);
//        ModConfig.CUSTOM_VOLUME.save();
    }
}