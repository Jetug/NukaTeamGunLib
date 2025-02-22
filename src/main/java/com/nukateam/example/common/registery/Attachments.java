package com.nukateam.example.common.registery;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.GunModifiers;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class Attachments {
    public static final ResourceLocation SCOPE_LOCATION = new ResourceLocation(Ntgl.MOD_ID, "textures/hud/overlay/scope_long_overlay.png");

    public static final Scope SHORT_SCOPE = Scope.builder()
            .aimFovModifier(0.7F)
            .modifiers(GunModifiers.SLOW_ADS)
            .build();

    public static final Scope MEDIUM_SCOPE = Scope.builder()
            .aimFovModifier(0.5F)
            .modifiers(GunModifiers.SLOW_ADS)
            .build();

    public static final Scope LONG_SCOPE = Scope.builder()
            .aimFovModifier(0.25F)
            .modifiers(GunModifiers.SLOWER_ADS)
            .overlay(SCOPE_LOCATION)
            .build();
}
