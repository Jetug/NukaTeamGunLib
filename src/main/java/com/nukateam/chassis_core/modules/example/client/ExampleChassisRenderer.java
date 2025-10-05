package com.nukateam.chassis_core.modules.example.client;

import com.nukateam.chassis_core.client.render.renderers.ChassisRenderer;
import com.nukateam.chassis_core.modules.example.common.entities.ExampleChassis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ExampleChassisRenderer extends ChassisRenderer<ExampleChassis> {
    public ExampleChassisRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ExampleChassisModel());
    }
}
