package com.nukateam.gunscore.common.foundation.item;

import net.minecraft.world.item.Item;

/**
 * A basic item class that implements {@link IAmmo} to indicate this item is ammunition
 * <p>
 * Author: MrCrayfish
 */
public class AmmoItem extends Item implements IAmmo {
    public AmmoItem(Properties properties) {
        super(properties);
    }
}
