package com.nukateam.ntgl.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.event.InputEvents;
import com.nukateam.ntgl.client.render.screen.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends Screen {
    @Shadow private OptionsList list;
//    private CustomVolumeSlider customVolumeSlider;

    protected SoundOptionsScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(
                Component.translatable("ntgl.options.gun_volume_button"),
                button -> this.minecraft.setScreen(new CustomVolumeScreen(this))
        ).bounds(this.width / 2 + 105 + InputEvents.X, this.height - 27 + InputEvents.Y, 100, 20).build());
    }
}