package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.ExplosionConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class WeaponItemTooltips {
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(
            new DecimalFormat("#.##"),
            (format) -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));

    public static void addExplosionTip(List<Component> tooltip, ExplosionConfig explosion) {
        var damage = explosion.getDamage();
        tooltip.add(Component.translatable("info.ntgl.explosionDamage",
                        ChatFormatting.WHITE + ATTRIBUTE_MODIFIER_FORMAT.format(damage))
                .withStyle(ChatFormatting.GRAY));

        var radius = explosion.getRadius();
        tooltip.add(Component.translatable("info.ntgl.explosionRadius",
                        ChatFormatting.WHITE + ATTRIBUTE_MODIFIER_FORMAT.format(radius))
                .withStyle(ChatFormatting.GRAY));
    }

    public static void addAmmoType(List<Component> tooltip, WeaponData data) {
        var descriptionId = WeaponStateHelper.getCurrentAmmo(data).getDescriptionId();

        tooltip.add(Component.translatable("info.ntgl.ammo_type",
                        Component.translatable(descriptionId).withStyle(ChatFormatting.WHITE)
                ).withStyle(ChatFormatting.GRAY));
    }

    public static void addFireRate(List<Component> tooltip, WeaponData data) {
        var rate = WeaponModifierHelper.getRate(data);
        rate = rate == 0 ? 0 : 20 / rate;

        tooltip.add(Component.translatable("info.ntgl.rate",
                ChatFormatting.WHITE + ATTRIBUTE_MODIFIER_FORMAT.format(rate))
                .withStyle(ChatFormatting.GRAY));
    }

    public static void addFuel(List<Component> tooltip, WeaponData weaponData) {
        var allFuel = WeaponModifierHelper.getAllFuel(weaponData);
        for (var fuelType : allFuel) {
            var fuelAmount = FuelUtils.getFuel(weaponData.weapon, fuelType);
            var maxFuel = WeaponModifierHelper.getMaxFuel(fuelType.getId(), weaponData);

            tooltip.add(Component.translatable(fuelType.getDescriptionId())
                    .append(ChatFormatting.WHITE + " : " + fuelAmount + "/" + maxFuel)
                    .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    public static void addAmmo(List<Component> tooltip, WeaponData weaponData) {
        if (WeaponStateHelper.isAmmoIgnored(weaponData)) {
            tooltip.add(Component.translatable("info.ntgl.ignore_ammo").withStyle(ChatFormatting.AQUA));
        } else {
            int ammoCount = WeaponStateHelper.getAmmoCount(weaponData);
            tooltip.add(Component.translatable("info.ntgl.ammo",
                    ChatFormatting.WHITE.toString()
                            + ammoCount + "/"
                            + WeaponModifierHelper.getMaxAmmo(weaponData)).withStyle(ChatFormatting.GRAY));
        }
    }

    public static void addDamage(List<Component> tooltip, WeaponData weaponData) {
        var damage = WeaponStateHelper.getProjectileDamage(weaponData);
        tooltip.add(Component.translatable("info.ntgl.damage", ChatFormatting.WHITE
                        + ATTRIBUTE_MODIFIER_FORMAT.format(damage)
        ).withStyle(ChatFormatting.GRAY));
    }

    public static void addMelleDamage(List<Component> tooltip, WeaponData weaponData) {
        var damage = WeaponModifierHelper.getMeleeDamage(weaponData);

        tooltip.add(Component.translatable("info.ntgl.melee_damage",
                ChatFormatting.WHITE + ATTRIBUTE_MODIFIER_FORMAT.format(damage)
        ).withStyle(ChatFormatting.GRAY));
    }
}
