package com.nukateam.example.common.modifiers;

import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import com.nukateam.ntgl.common.data.WeaponData;

public class TestModifier implements IWeaponModifier {
    @Override
    public int modifyReloadTime(int reloadTime, WeaponData data) {
//        var mainHandStack = data.shooter.getMainHandItem();
//
//        if(ItemStack.matches(mainHandStack, data.gun)) {
//            return 1;
//        }
//        else return reloadTime;
        return reloadTime;
    }
}
