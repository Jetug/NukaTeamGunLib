package com.nukateam.ntgl.common.util.world;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.config.weapon.ExplosionConfig;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageProjectileExplosion;
import com.nukateam.ntgl.common.util.interfaces.IExplosionDamageable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

public class ExplosionUtils {
    public static boolean isExplosive(ExplosionConfig config) {
        return config.getRadius() > 0;
    }

    public static void createExplosion(@NotNull Entity entity, @NotNull ExplosionConfig config, Vec3 hitPos) {
        var world = entity.level();
        if (world.isClientSide() || hitPos == null || config.getRadius() <= 0)
            return;

        entity.setPos(hitPos);

        var source = entity instanceof ProjectileEntity projectileEntity ?
                entity.damageSources().explosion(entity, projectileEntity.getShooter()) :
                null;

        var mode = config.isDestroyBlocks() && Config.COMMON.gameplay.griefing.enableBlockRemovalOnExplosions.get() ?
                Explosion.BlockInteraction.DESTROY :
                Explosion.BlockInteraction.KEEP;

        var explosion = new ProjectileExplosion(world,
                entity, source, null,
                config, hitPos, mode);

        if (ForgeEventFactory.onExplosionStart(world, explosion))
            return;

        explosion.explode();
        explosion.finalizeExplosion(true);

        explosion.getToBlow().forEach(pos ->
        {
            if (world.getBlockState(pos).getBlock() instanceof IExplosionDamageable) {
                ((IExplosionDamageable) world.getBlockState(pos).getBlock()).onProjectileExploded(world, world.getBlockState(pos), pos, entity);
            }
        });

        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for (var player : ((ServerLevel) world).players()) {
            if (player.distanceToSqr(hitPos) < 4096) {
                PacketHandler.getPlayChannel().sendToPlayer(() -> player,
                        new S2CMessageProjectileExplosion(
                                hitPos, explosion.getHitPlayers().get(player),
                                config, explosion.getToBlow())
                );
            }
        }
    }
}
