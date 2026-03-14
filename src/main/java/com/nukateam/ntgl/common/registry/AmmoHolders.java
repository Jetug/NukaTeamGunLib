package com.nukateam.ntgl.common.registry;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static com.nukateam.ntgl.Ntgl.ntglResource;

public class AmmoHolders {
    public static final AmmoHolder EMPTY = AmmoHolder.getType("minecraft:air");

    public static final AmmoHolder BURNABLE = AmmoHolder.Builder
            .create(ntglResource("burnable"))
            .isAcceptable(AmmoHolders::isBurnable)
            .value((stack -> stack.getBurnTime(null)))
            .onConsume(AmmoHolders::consumeBurnable)
            .build();

    public static final AmmoHolder WATER = AmmoHolder.Builder
            .create(ntglResource("water"))
            .isAcceptable(AmmoHolders::isWater)
            .value((stack -> 1000))
            .onConsume(AmmoHolders::consumeWater)
            .build();

    public static void register() {
        AmmoHolder.registerType(BURNABLE);
        AmmoHolder.registerType(WATER);
    }

    public static boolean isBurnable(ItemStack ammoStack) {
        var burnTime = ammoStack.getBurnTime(null);
        return burnTime > 0;
    }

    public static boolean isWater(ItemStack ammoStack) {
        return ammoStack.getItem() == Items.WATER_BUCKET;
    }

    public static List<ItemStack> consumeBurnable(ItemStack stack, Integer i) {
        if (stack.getItem() == Items.LAVA_BUCKET) {
            return List.of(new ItemStack(Items.BUCKET));
        }
        return List.of();
    }

    public static List<ItemStack> consumeWater(ItemStack stack, Integer i) {
        if (stack.getItem() == Items.WATER_BUCKET) {
            return List.of(new ItemStack(Items.BUCKET));
        }
        return List.of();
    }
}
