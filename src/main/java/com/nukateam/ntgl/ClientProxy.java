package com.nukateam.ntgl;

import com.nukateam.ntgl.common.base.utils.DeathType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ClientProxy {
    //    public static TGParticleManager particleManager = new TGParticleManager();
    public static final int MAX_TICKS = 20 * 5;

    public static Map<Integer, Ses> damageTypes = new HashMap<>();

    public static void setDamageType(@NotNull Entity entity, DeathType damageType) {
        ClientProxy.damageTypes.put(entity.getId(), new Ses(damageType, MAX_TICKS));
    }

    @Nullable
    public static DeathType getDamageType(Entity entity) {
        var ses = ClientProxy.damageTypes.get(entity.getId());
        return ses == null ? null : ses.deathType;
    }

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
        return (LivingEntityRenderer<? super LivingEntity, ? extends EntityModel<? extends LivingEntity>>) getEntityRenderer(entity);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            var buffMap = new HashMap<>(damageTypes);

            buffMap.forEach((key, value) -> {
                if (value.ticks <= 0) {
                    damageTypes.remove(key);
                }
                value.ticks--;
            });
        }
    }

    public static class Ses {
        public int ticks;
        public DeathType deathType;

        public Ses(DeathType damageType, int maxTicks) {
            this.ticks = maxTicks;
            this.deathType = damageType;
        }
    }

//    public static ClientProxy get(){
//        return (ClientProxy) Techguns.proxy;
//    }
}
