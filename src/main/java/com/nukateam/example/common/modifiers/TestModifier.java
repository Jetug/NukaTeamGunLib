package com.nukateam.example.common.modifiers;

import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.world.item.ItemStack;

public class TestModifier implements IGunModifier {
    @Override
    public int modifyReloadTime(int reloadTime, GunData data) {
//        var mainHandStack = data.shooter.getMainHandItem();
//
//        if(ItemStack.matches(mainHandStack, data.gun)) {
//            return 1;
//        }
//        else return reloadTime;
        return reloadTime;
    }
}
