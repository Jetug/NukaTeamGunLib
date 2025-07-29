package com.nukateam.ntgl.modules.enchantment;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Author: MrCrayfish
 */
public class EnchantmentTypes {
    public static final EnchantmentCategory GUN = EnchantmentCategory.create(Ntgl.MOD_ID + ":gun", item ->
            item instanceof WeaponItem weaponItem && weaponItem.getGun().getGeneral().isEnchantable() &&
                    weaponItem.getGun().getGeneral().getWeaponMode() == WeaponMode.GUN
    );
    public static final EnchantmentCategory SEMI_AUTO_GUN = EnchantmentCategory.create(Ntgl.MOD_ID + ":semi_auto_gun", item ->
            item instanceof WeaponItem && ((WeaponItem) item).getGun().getGeneral().getFireModes().stream().noneMatch((v) -> v == FireMode.AUTO));
}
