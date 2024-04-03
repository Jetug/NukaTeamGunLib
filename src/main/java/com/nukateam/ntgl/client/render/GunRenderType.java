package com.nukateam.ntgl.client.render;

import com.nukateam.ntgl.Ntgl;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * Author: MrCrayfish
 */

public final class GunRenderType extends RenderType {
    private static final RenderType BULLET_TRAIL = RenderType.create(Ntgl.MOD_ID + ":projectile_trail",
            DefaultVertexFormat
                    .POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setCullState(NO_CULL)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(false));
    @Deprecated(since = "1.3.0", forRemoval = true)
    private static final RenderType SCREEN = RenderType.create(Ntgl.MOD_ID + ":screen_texture",
            DefaultVertexFormat
                    .NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.NEW_ENTITY_SHADER)
                    .setTexturingState(ScreenTextureState.instance())
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY).createCompositeState(false));

    private GunRenderType(String nameIn, VertexFormat formatIn, VertexFormat.Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static RenderType getBulletTrail() {
        return BULLET_TRAIL;
    }

    @Deprecated(since = "1.3.0", forRemoval = true)
    public static RenderType getScreen() {
        return SCREEN;
    }
}
