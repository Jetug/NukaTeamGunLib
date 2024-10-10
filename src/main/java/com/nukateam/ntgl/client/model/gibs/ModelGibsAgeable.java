package com.nukateam.ntgl.client.model.gibs;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.common.data.interfaces.IAgeableAccessor;
import com.nukateam.ntgl.common.data.interfaces.IModelAccessor;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

public class ModelGibsAgeable extends ModelGibs {
    private final AgeableListModel model;
    private final IModelAccessor accessor;

    public ModelGibsAgeable(AgeableListModel model) {
        this.model = model;
        this.accessor = (IModelAccessor)model;
        accessor.getModelParts().forEach(part -> part.setRotation(0.0f, 0.0f, 0.0f));
    }

    @Override
    public void render(Entity entity, int part, PoseStack poseStack, VertexConsumer pVertexConsumer, int packedLight, int packedOverlay) {
        var ageableAccessor = (IAgeableAccessor)model;
        var isHead = new ArrayList<Boolean>();
        ageableAccessor.getHeadParts().forEach((val) -> isHead.add(true));
        ageableAccessor.getBodyParts().forEach((val) -> isHead.add(false));

        var livingEntity = (LivingEntity)entity;

        poseStack.pushPose();
        {
            if(livingEntity.isBaby()) {
                if (isHead.get(part)) {
                    if (ageableAccessor.isScaleHead()) {
                        var scale = 1.5F / ageableAccessor.getBabyHeadScale();
                        poseStack.scale(scale, scale, scale);
                    }

                    poseStack.translate(0.0F, ageableAccessor.getBabyYHeadOffset() / 16.0F, ageableAccessor.getBabyZHeadOffset() / 16.0F);
                } else {
                    var scale = 1.0F / ageableAccessor.getBabyBodyScale();
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0.0F, ageableAccessor.getBodyYOffset() / 16.0F, 0.0F);
                }
            }
            accessor.getModelParts().get(part).render(poseStack, pVertexConsumer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }


    @Override
    public int getNumGibs() {
        return accessor.getModelParts().size();
    }
}
