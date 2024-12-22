package com.nukateam.example.client;

import com.nukateam.example.common.entities.Raider;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DeathclawRenderer extends GeoEntityRenderer<Deathclaw> {
    public static final DeathclawModel<Deathclaw> DEATHCLAW_MODEL = new DeathclawModel<>();

    public DeathclawRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, DEATHCLAW_MODEL);
    }
}