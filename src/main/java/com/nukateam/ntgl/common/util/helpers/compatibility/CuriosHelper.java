package com.nukateam.ntgl.common.util.helpers.compatibility;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class CuriosHelper {
    @Nullable
    public static ItemStack getItem(Player player, Predicate<ItemStack> filter) {
        ICuriosItemHandler capability = player.getCapability(CuriosCapability.INVENTORY);
        if(capability == null) return null;
        var backpackCurio = capability.findFirstCurio(filter);

        if(backpackCurio.isPresent())
            return backpackCurio.get().stack();
        else return null;
    }
}
