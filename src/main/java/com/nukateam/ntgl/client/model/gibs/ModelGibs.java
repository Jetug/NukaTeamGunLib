package com.nukateam.ntgl.client.model.gibs;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public abstract class ModelGibs extends Model {

	public ModelGibs(Function<ResourceLocation, RenderType> pRenderType) {
		super(pRenderType);
	}

	public abstract void render(Entity entityIn, float scale, int part);
	
	public abstract int getNumGibs();
}
