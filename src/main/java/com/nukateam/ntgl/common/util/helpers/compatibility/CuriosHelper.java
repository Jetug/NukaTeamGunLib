package com.nukateam.ntgl.common.util.helpers.compatibility;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class CuriosHelper {
    @Nullable
    public static ItemStack getItem(Player player, Predicate<ItemStack> filter) {
        var backpackCurio = CuriosApi.getCuriosHelper().findFirstCurio(player, filter);

        if(backpackCurio.isPresent())
            return backpackCurio.get().stack();
        else return null;
    }
}
