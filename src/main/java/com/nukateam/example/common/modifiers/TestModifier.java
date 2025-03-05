package com.nukateam.example.common.modifiers;

import com.nukateam.ntgl.common.base.DynamicGunModifier;
import net.minecraft.world.item.ItemStack;

public class TestModifier extends DynamicGunModifier {
    @Override
    public int modifyReloadTime(int reloadTime) {
        var mainHandStack = getEntity().getMainHandItem();

        if(ItemStack.matches(mainHandStack, stack)) {
            return 1;
        }
        else return reloadTime;
    }
}
