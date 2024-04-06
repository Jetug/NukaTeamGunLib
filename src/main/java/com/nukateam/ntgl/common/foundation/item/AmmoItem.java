package com.nukateam.ntgl.common.foundation.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A basic item class that implements {@link IAmmo} to indicate this item is ammunition
 * <p>
 * Author: MrCrayfish
 */
public class AmmoItem extends Item implements IAmmo {
    public AmmoItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        var maxDamage = getMaxDamage(stack);



        if(maxDamage > 0){
            int ammoCount = maxDamage - getDamage(stack);
            tooltip.add(Component.translatable("info.ntgl.ammo", ChatFormatting.WHITE.toString() + ammoCount + "/" + maxDamage).withStyle(ChatFormatting.GRAY));
        }
    }
}
