package com.nukateam.example.common.modifiers;

import com.nukateam.ntgl.common.base.DynamicGunModifier;
import com.nukateam.ntgl.common.base.holders.GripType;
import com.nukateam.ntgl.common.util.util.GunData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

public class MinigunModifier extends DynamicGunModifier {
    @Override
    public int modifyReloadTime(int reloadTime, GunData data) {
        var mainHandStack = getEntity().getMainHandItem();

        if(ItemStack.matches(mainHandStack, stack)) {
            return 1;
        }
        else return reloadTime;
    }

    @Override
    public GripType modifyGripType(GripType gripType, GunData data) {
        if(getEntity().hasEffect(MobEffects.DAMAGE_BOOST)){
            return GripType.ONE_HANDED;
        }
        return super.modifyGripType(gripType, data);
    }
}
