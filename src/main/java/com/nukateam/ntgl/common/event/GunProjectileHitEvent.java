package com.nukateam.ntgl.common.event;

import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * <p>Fired when a ammo hits a block or entity.</p>
 *
 * @author Ocelot
 */
@Cancelable
public class GunProjectileHitEvent extends Event {
    private final HitResult result;
    private final ProjectileEntity projectile;

    public GunProjectileHitEvent(HitResult result, ProjectileEntity projectile) {
        this.result = result;
        this.projectile = projectile;
    }

    /**
     * @return The result of the entity's ray trace
     */
    public HitResult getRayTrace() {
        return result;
    }

    /**
     * @return The ammo that hit
     */
    public ProjectileEntity getProjectile() {
        return projectile;
    }
}
