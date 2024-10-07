package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.mixin.client.LivingEntityModelMixin;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ModelGibsGeneric extends ModelGibs{
	Model model;

	List<ModelPart> gibs = new ArrayList<ModelPart>();


	public ModelGibsGeneric(HierarchicalModel model) {
		this.model = model;

//		HashSet<ModelRenderer> childBoxes = new HashSet<ModelRenderer>(64);



        gibs = model.root().getAllParts().toList();


//        for (Object o : model.root().getAllParts()) {
//        	ModelRenderer box = (ModelRenderer)o;
//        	if (box.childModels != null) {
//        		childBoxes.addAll(box.childModels);
//        	}
//        }


        for(var part : gibs){
            part.setRotation(0.0f, 0.0f, 0.0f);
        }


//        for (Object o : model.boxList) {
//        	ModelRenderer box = (ModelRenderer)o;
//        	if (!childBoxes.contains(box) && !box.isHidden && box.showModel) {
////        		if (box.cubeList.size() >= 1) {
////    				ModelBox mb = box.cubeList.get(0);
////    				float dx = Math.abs(mb.posX1-mb.posX2);
////    				float dy = Math.abs(mb.posY1-mb.posY2);
////    				float dz = Math.abs(mb.posZ1-mb.posZ2);
////    				box.offsetX = -dx*0.5f;
////    				box.offsetY = -dy*0.5f;
////    				box.offsetZ = -dz*0.5f;
////    			}
//    			box.setRotationPoint(0.0f, 0.0f, 0.0f);
//    			gibs.add(box);
//        	}
//        }

//		for (Object o : this.model.boxList) {
//			ModelRenderer box  = (ModelRenderer)o;
//			if (box.cubeList.size() >= 1) {
//				ModelBox mb = box.cubeList.get(0);
//				float dx = Math.abs(mb.posX1-mb.posX2);
//				float dy = Math.abs(mb.posY1-mb.posY2);
//				float dz = Math.abs(mb.posZ1-mb.posZ2);
//				box.offsetX = -dx*0.5f;
//				box.offsetY = -dy*0.5f;
//				box.offsetZ = -dz*0.5f;
//			}
//			box.setRotationPoint(0.0f, 0.0f, 0.0f);
//		}
	}

//	@Override
//	public void render(Entity entity, float scale, int part) {
//		gibs.get(part).render(scale);
////		int i = 0;
////		for (Object o : this.model.boxList) {
////			ModelRenderer box  = (ModelRenderer)o;
////			if (i++ == part) {
////				box.render(scale);
////			}
////		}
//	}

    @Override
    public void render(Entity entity, int part, PoseStack pPoseStack, VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay) {
        gibs.get(part).render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);
    }

    public int getNumGibs() {
		return gibs.size(); //this.model.boxList.size();
	}
}
