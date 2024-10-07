package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public abstract class ModelGibs/* extends Model */{

//	public ModelGibs(Function<ResourceLocation, RenderType> pRenderType) {
//		super(pRenderType);
//	}

	public abstract void render(Entity entity, int part, PoseStack pPoseStack, VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay);
	
	public abstract int getNumGibs();
}
