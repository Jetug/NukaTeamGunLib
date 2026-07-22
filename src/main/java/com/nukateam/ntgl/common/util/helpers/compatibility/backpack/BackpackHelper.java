package com.nukateam.ntgl.common.util.helpers.compatibility.backpack;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.IAmmoContext;
import net.minecraft.world.entity.player.Player;

public class BackpackHelper {
    public static IAmmoContext findAmmo(Player player, AmmoHolder id) {
        try {
            var context = (IAmmoContext)AmmoContext.NONE;

            if (Ntgl.backpackedLoaded && Ntgl.curiosLoaded) {
                context = BackpackedHelper.findAmmo(player, id);
            }
            if(Ntgl.sophisticatedLoaded && context.equals(AmmoContext.NONE)){
                context = SophisticatedHelper.findAmmo(player, id);
            }
            if(Ntgl.travelersLoaded && context.equals(AmmoContext.NONE)){
                context = TravelersHelper.findAmmo(player, id);
            }
            if(Ntgl.yyzBackpackLoaded && context.equals(AmmoContext.NONE)){
                context = YyzBackpackHelper.findAmmo(player, id);
            }
            return context;
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
            return AmmoContext.NONE;
        }
    }

    public static IAmmoContext findMagazine(Player player, AmmoHolder id) {
        try {
            var context = (IAmmoContext) AmmoContext.NONE;

            if (Ntgl.backpackedLoaded && Ntgl.curiosLoaded) {
                context = BackpackedHelper.findMagazine(player, id);
            }
            if (Ntgl.sophisticatedLoaded && context.equals(AmmoContext.NONE)) {
                context = SophisticatedHelper.findMagazine(player, id);
            }
            if (Ntgl.travelersLoaded && context.equals(AmmoContext.NONE)) {
                context = TravelersHelper.findMagazine(player, id);
            }
            if (Ntgl.yyzBackpackLoaded && context.equals(AmmoContext.NONE)) {
                context = YyzBackpackHelper.findMagazine(player, id);
            }

            return context;
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
            return AmmoContext.NONE;
        }
    }
}
