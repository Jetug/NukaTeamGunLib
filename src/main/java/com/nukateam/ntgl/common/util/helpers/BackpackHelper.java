package com.nukateam.ntgl.common.util.helpers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.AmmoContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class BackpackHelper {
    public static AmmoContext findAmmo(Player player, ResourceLocation id) {
        var context = AmmoContext.NONE;

        if (Ntgl.backpackedLoaded) {
            context = BackpackedHelper.findAmmo(player, id);
        }
        if(Ntgl.sophisticatedLoaded && context.equals(AmmoContext.NONE)){
            context = SophisticatedHelper.findAmmo(player, id);
        }

        return context;
    }

    public static AmmoContext findMagazine(Player player, ResourceLocation id) {
        var context = AmmoContext.NONE;

        if (Ntgl.backpackedLoaded) {
            context = BackpackedHelper.findMagazine(player, id);
        }
        if(Ntgl.sophisticatedLoaded && context.equals(AmmoContext.NONE)){
            context = SophisticatedHelper.findMagazine(player, id);
        }

        return context;
    }
}
