package com.nukateam.ntgl.client.render.renderers.weapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.util.TransformUtils;
import com.nukateam.ntgl.client.util.util.render.ModelRenderUtil;
import com.nukateam.ntgl.common.util.data.Rgba;
import com.nukateam.ntgl.common.util.helpers.compatibility.ChassisHelper;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.util.ClientUtils;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderLeftArm;
import static com.nukateam.ntgl.client.render.GeoRenderUtils.renderRightArm;

public class ThrowableItemRenderer<Animator extends ItemAnimator> extends ArmsRenderer<Animator> {
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    protected MultiBufferSource bufferSource;
    protected ItemStack stack;
    protected boolean firstRightRender = true;
    protected boolean firstLeftRender = true;
    private ItemDisplayContext transformType;

    public ThrowableItemRenderer(GeoModel<Animator> model) {
        super(model);
    }

    @Override
    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.stack = stack;
        this.firstRightRender = true;
        this.firstLeftRender  = true;
        this.currentEntity = entity;

        poseStack.pushPose();
        {
            if(TransformUtils.isFirstPerson(transformType)){
                poseStack.translate(0, -6 / 16D, 0);
            }
            else if (TransformUtils.isThirdPerson(transformType)){
                poseStack.translate(0, -9 / 16D, 0);
            }
            else if(TransformUtils.isNonHand(transformType)){
                poseStack.translate(0, -8 / 16D, 0);
            }
            else {
                poseStack.translate(0, -8 / 16D, 0);
                poseStack.translate(0, ClientDebug.Y / 10d / 16D, 0);
            }
            super.render(entity, stack, transformType, poseStack, bufferSource, renderType, buffer, packedLight);
        }
        poseStack.popPose();
    }
}
