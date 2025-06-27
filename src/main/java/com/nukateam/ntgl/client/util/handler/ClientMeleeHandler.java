package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

/**
 * Author: MrCrayfish
 */
public class ClientMeleeHandler {
    private static ClientMeleeHandler instance;

    private int startReloadTick;
    private int reloadTimer;
    private int prevReloadTimer;
    private int reloadingSlot;
    private int reloadTicks;

    private ClientMeleeHandler() {
    }

    public static boolean isDoingMelee(LivingEntity shooter, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ?
                ModSyncedDataKeys.MELEE_RIGHT.getValue(shooter) :
                ModSyncedDataKeys.MELEE_LEFT.getValue(shooter);
    }
}
