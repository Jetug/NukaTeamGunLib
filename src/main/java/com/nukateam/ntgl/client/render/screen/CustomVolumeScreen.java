package com.nukateam.ntgl.client.render.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import static net.minecraft.network.chat.Component.translatable;

public class CustomVolumeScreen extends Screen {
    private final Screen parent;

    public CustomVolumeScreen(Screen parent) {
        super(translatable("options.custom_volume"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new CustomVolumeSlider(
                this.width / 2 - 100, this.height / 2 - 30, 200, 20, Config.CLIENT.sounds.gunVolume
        ));

        this.addRenderableWidget(Button.builder(
                translatable("gui.back"),
                button -> this.minecraft.setScreen(this.parent)
        ).bounds(this.width / 2 - 100, this.height / 2 + 30, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);
    }

    // Кастомный слайдер
    private static class CustomVolumeSlider extends AbstractSliderButton {
        private final ForgeConfigSpec.DoubleValue configValue;

        public CustomVolumeSlider(int x, int y, int width, int height, ForgeConfigSpec.DoubleValue configValue) {
            super(x, y, width, height, translatable("ntgl.options.gun_volume", configValue.get()), configValue.get());
            this.configValue = configValue;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            var message
                    = translatable("ntgl.options.gun_volume", (int)(this.value * 100) + "%");
            this.setMessage(message);
        }

        @Override
        protected void applyValue() {
            try {
                this.configValue.set(this.value);
                this.configValue.save();
            }
            catch (com.electronwill.nightconfig.core.io.WritingException e){
                Ntgl.LOGGER.error("Failed to save config", e);
            }
        }
    }
}
