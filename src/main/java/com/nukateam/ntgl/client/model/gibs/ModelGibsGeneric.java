package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ModelGibsGeneric extends ModelGibs{
    private List<ModelPart> gibs;

	public ModelGibsGeneric(HierarchicalModel model) {
        gibs = model.root().getAllParts().toList();

        for(var part : gibs){
            part.setRotation(0.0f, 0.0f, 0.0f);
        }
	}

    @Override
    public void render(Entity entity, int part, PoseStack poseStack, VertexConsumer pVertexConsumer, int packedLight, int packedOverlay) {
        if (part != 0)
            gibs.get(part).render(poseStack, pVertexConsumer, packedLight, packedOverlay);
    }

    public int getNumGibs() {
		return gibs.size(); //this.model.boxList.size();
	}
}
