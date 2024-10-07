package com.nukateam.ntgl.client.model.gibs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;

import static com.nukateam.ntgl.ClientProxy.getEntityRenderer;

public class ModelGibsBiped extends ModelGibs {
    public HumanoidModel model;

    public ModelGibsBiped(HumanoidModel model) {
        this.model = model;
        this.model.head.setRotation(0.0f, 0.0f, 0.0f);
        this.model.body.setRotation(0.0f, 0.0f, 0.0f);
        this.model.rightArm.setRotation(0.0f, 0.0f, 0.0f);
        this.model.leftArm.setRotation(0.0f, 0.0f, 0.0f);
        this.model.rightLeg.setRotation(0.0f, 0.0f, 0.0f);
        this.model.leftLeg.setRotation(0.0f, 0.0f, 0.0f);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entity, int part, PoseStack pPoseStack, VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay) {
        var s = (HumanoidModel) (Object) getEntityRenderer(entity);

        switch (part) {
            case 0:
                this.model.head.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);
                break;
            case 1:
                this.model.body.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);
                break;
            case 2:
                this.model.rightArm.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);
                break;
            case 3:
                this.model.leftArm.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);
                break;
            case 4:
                this.model.rightLeg.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);
                break;
            case 5:
                this.model.leftLeg.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);
                break;
        }
    }

    @Override
    public int getNumGibs() {
        return 6;
    }
}