package com.nukateam.ntgl.common.data.holders;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeHooks;

import static com.nukateam.ntgl.Ntgl.ntglResource;

public class AmmoHolders {
    public static final AmmoHolder BURNABLE = AmmoHolder.Builder
            .create(ntglResource("burnable"))
            .setIsAcceptable(AmmoHolders::isBurnable)
            .setGetValue((stack -> ForgeHooks.getBurnTime(stack, null)))
            .build();

    public static final AmmoHolder WATER = AmmoHolder.Builder
            .create(ntglResource("water"))
            .setIsAcceptable(AmmoHolders::isWater)
            .setGetValue((stack -> 1000))
            .build();

    public static void register() {
        AmmoHolder.registerType(BURNABLE);
        AmmoHolder.registerType(WATER);
    }

    private static boolean isBurnable(ItemStack ammoStack) {
        var burnTime = ForgeHooks.getBurnTime(ammoStack, null);
        return burnTime > 0;
    }

    private static boolean isWater(ItemStack ammoStack) {
        return ammoStack.getItem() == Items.WATER_BUCKET;
    }
}
