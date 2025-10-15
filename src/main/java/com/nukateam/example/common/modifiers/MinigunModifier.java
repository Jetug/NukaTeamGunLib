package com.nukateam.example.common.modifiers;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.GripType;
import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

public class MinigunModifier implements IWeaponModifier {
    @Override
    public int modifyReloadTime(int reloadTime, WeaponData data) {
        var mainHandStack = data.wielder.getMainHandItem();

        if(ItemStack.matches(mainHandStack, data.weapon)) {
            return 1;
        }
        else return reloadTime;
    }

    @Override
    public GripType modifyGripType(GripType gripType, WeaponData data) {
        if(data.wielder.hasEffect(MobEffects.DAMAGE_BOOST)){
            return GripType.ONE_HANDED;
        }
        return IWeaponModifier.super.modifyGripType(gripType, data);
    }
}
