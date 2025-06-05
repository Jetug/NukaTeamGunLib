package com.nukateam.ntgl.common.util.helpers.compatibility;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.IAmmoContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class BackpackHelper {
    public static IAmmoContext findAmmo(Player player, ResourceLocation id) {
        var context = (IAmmoContext)AmmoContext.NONE;

        if (Ntgl.backpackedLoaded) {
            context = BackpackedHelper.findAmmo(player, id);
        }
        if(Ntgl.sophisticatedLoaded && Ntgl.curiosLoaded && context.equals(AmmoContext.NONE)){
            context = SophisticatedHelper.findAmmo(player, id);
        }

        return context;
    }

    public static IAmmoContext findMagazine(Player player, ResourceLocation id) {
        var context = (IAmmoContext)AmmoContext.NONE;

        if (Ntgl.backpackedLoaded && Ntgl.curiosLoaded) {
            context = BackpackedHelper.findMagazine(player, id);
        }
        if(Ntgl.sophisticatedLoaded && context.equals(AmmoContext.NONE)){
            context = SophisticatedHelper.findMagazine(player, id);
        }

        return context;
    }
}
