package com.nukateam.ntgl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ClientProxy {
//    public static TGParticleManager particleManager = new TGParticleManager();

    @NotNull
    public static BlockPos getEntityBlockPos(Entity entity) {
        return new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ()));
    }

    public static EntityRenderer getEntityRenderer(EntityType entity) {
        return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity.create(Minecraft.getInstance().level));
    }

    public static EntityRenderer getEntityRenderer(Entity entity) {
        return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
    }

    public static LivingEntityRenderer<? super LivingEntity, ? extends EntityModel<? extends LivingEntity>> getLivingEntityRenderer(Entity entity) {
        return (LivingEntityRenderer<? super LivingEntity, ? extends EntityModel<? extends LivingEntity>>)getEntityRenderer(entity);
    }

//    public static ClientProxy get(){
//        return (ClientProxy) Techguns.proxy;
//    }
}
