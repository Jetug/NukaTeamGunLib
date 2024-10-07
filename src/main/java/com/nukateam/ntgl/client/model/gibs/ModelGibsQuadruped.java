package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.common.data.interfaces.IModelAccessor;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.world.entity.Entity;

public class ModelGibsQuadruped extends ModelGibs {
    public QuadrupedModel model;

    public ModelGibsQuadruped(QuadrupedModel model) {
        this.model = model;
        var accessor = (IModelAccessor)model;
        accessor.getModelParts().forEach(part -> part.setRotation(0.0f, 0.0f, 0.0f));

//        this.model.head.setRotationPoint(0.0f, 0.0f, 0.0f);
//        this.model.body.setRotationPoint(0.0f, 0.0f, 0.0f);
//        this.model.leg1.setRotationPoint(0.0f, 0.0f, 0.0f);
//        this.model.leg2.setRotationPoint(0.0f, 0.0f, 0.0f);
//        this.model.leg3.setRotationPoint(0.0f, 0.0f, 0.0f);
//        this.model.leg4.setRotationPoint(0.0f, 0.0f, 0.0f);
    }


    @Override
    public void render(Entity entityIn, int part, PoseStack pPoseStack, VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay) {
        var accessor = (IModelAccessor)model;
        accessor.getModelParts().get(part).render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);

//        switch (part) {
//            case 0:
//                this.model.head.render(scale);
//                break;
//            case 1:
//                this.model.body.render(scale);
//                break;
//            case 2:
//                this.model.leg1.render(scale);
//                break;
//            case 3:
//                this.model.leg2.render(scale);
//                break;
//            case 4:
//                this.model.leg3.render(scale);
//                break;
//            case 5:
//                this.model.leg4.render(scale);
//                break;
//        }
    }

    @Override
    public int getNumGibs() {
        return 6;
    }
}
