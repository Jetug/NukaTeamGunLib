package com.nukateam.ntgl;

import com.nukateam.ntgl.common.data.enums.DeathType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ClientProxy {
    public static final int MAX_TICKS = 20 * 5;
    public static Map<Integer, DeathData> damageTypes = new HashMap<>();

    public static void setDamageType(@NotNull Entity entity, DeathType damageType) {
        ClientProxy.damageTypes.put(entity.getId(), new DeathData(damageType, MAX_TICKS));
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

    public static EntityRenderer getEntityRenderer(Entity entity) {
        return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
    }

    public static class DeathData {
        public int ticks;
        public DeathType deathType;

        public DeathData(DeathType damageType, int maxTicks) {
            this.ticks = maxTicks;
            this.deathType = damageType;
        }
    }
}
