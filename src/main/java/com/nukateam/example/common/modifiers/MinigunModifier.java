package com.nukateam.example.common.modifiers;

import com.nukateam.ntgl.common.data.holders.GripType;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

public class MinigunModifier implements IGunModifier {
    @Override
    public int modifyReloadTime(int reloadTime, GunData data) {
        var mainHandStack = data.shooter.getMainHandItem();

        if(ItemStack.matches(mainHandStack, data.gun)) {
            return 1;
        }
        else return reloadTime;
    }

    @Override
    public GripType modifyGripType(GripType gripType, GunData data) {
        if(data.shooter.hasEffect(MobEffects.DAMAGE_BOOST)){
            return GripType.ONE_HANDED;
        }
        return IGunModifier.super.modifyGripType(gripType, data);
    }
}
