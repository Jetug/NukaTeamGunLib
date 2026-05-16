package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.ExplosionConfig;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
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
        tooltip.add(Component.literal(" ")
                .append(Component.translatable("info.ntgl.explosionDamage",
                        Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)).withStyle(ChatFormatting.DARK_GREEN))
                .withStyle(ChatFormatting.GRAY)));

        var radius = explosion.getRadius();
        if (radius > 0) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.explosionRadius",
                            Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(radius)).withStyle(ChatFormatting.DARK_GREEN))
                    .withStyle(ChatFormatting.GRAY)));
        }
    }

    public static void addRangedStats(List<Component> tooltip, CompoundTag tagCompound, WeaponData data) {
        addAmmo(tooltip, tagCompound, data);
        addAmmoType(tooltip, data);
        addFireRate(tooltip, data);
        addDamage(tooltip, data);

        var spread = WeaponModifierHelper.getSpread(data);
        tooltip.add(Component.literal(" ")
                .append(Component.translatable("info.ntgl.spread",
                        Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(spread)).withStyle(ChatFormatting.DARK_GREEN))
                .withStyle(ChatFormatting.GRAY)));

        var fireMode = WeaponStateHelper.getFireMode(data);
        if (fireMode != null) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.fire_mode",
                            fireMode.getDisplayName().copy().withStyle(ChatFormatting.DARK_GREEN))
                    .withStyle(ChatFormatting.GRAY)));
        }
    }

    public static void addExplosionStats(List<Component> tooltip, WeaponData data, ProjectileConfig projectile) {
        var explosion = projectile.getExplosion();

        addExplosionTip(tooltip, explosion);

        var knockback = explosion.getKnockback();
        if (knockback > 0) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.knockback",
                            Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(knockback)).withStyle(ChatFormatting.DARK_GREEN))
                    .withStyle(ChatFormatting.GRAY)));
        }

        if (explosion.isExplodeOnContact()) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.explodes_on_contact"))
                    .withStyle(ChatFormatting.DARK_GREEN));
        } else {
            var delayTicks = WeaponModifierHelper.getProjectileLife(data, projectile.getLife());
            if (delayTicks > 0) {
                float fuseTime = delayTicks / 20.0f;
                tooltip.add(Component.literal(" ")
                        .append(Component.translatable("info.ntgl.fuse_time",
                                Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(fuseTime)).withStyle(ChatFormatting.DARK_GREEN))
                        .withStyle(ChatFormatting.GRAY)));
            }
        }

        if (explosion.isCauseFire()) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.causes_fire"))
                    .withStyle(ChatFormatting.DARK_GREEN));
        }

        if (explosion.isDestroyBlocks()) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.destroys_blocks"))
                    .withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    public static void addHandlingStats(List<Component> tooltip, WeaponData data, boolean showBash) {
        if (!WeaponModifierHelper.canShoot(data) && !showBash) return;

        tooltip.add(Component.translatable("info.ntgl.header.handling").withStyle(ChatFormatting.DARK_GRAY));

        if (WeaponModifierHelper.canShoot(data)) {
            var reloadTimeTicks = WeaponModifierHelper.getReloadTime(data);
            float reloadTime = reloadTimeTicks / 20.0f;
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.reload_time",
                            Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(reloadTime)).withStyle(ChatFormatting.DARK_GREEN))
                    .withStyle(ChatFormatting.GRAY)));
        }


        if (showBash) {
            tooltip.add(Component.translatable("info.ntgl.header.bash").withStyle(ChatFormatting.DARK_GRAY));

            var damage = WeaponModifierHelper.getMeleeDamage(data);
            var cooldown = WeaponModifierHelper.getMeleeCooldown(data);
            double speed = cooldown > 0 ? 20.0 / cooldown : 4.0;

            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("attribute.modifier.equals.0",
                            ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage),
                            Component.translatable("attribute.name.generic.attack_damage")))
                    .withStyle(ChatFormatting.DARK_GREEN));

            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("attribute.modifier.equals.0",
                            ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(speed),
                            Component.translatable("attribute.name.generic.attack_speed")))
                    .withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    public static void addVanillaMeleeStats(List<Component> tooltip, WeaponData data) {
        var damage = WeaponModifierHelper.getMeleeDamage(data);
        var cooldown = WeaponModifierHelper.getMeleeCooldown(data);
        double speed = cooldown > 0 ? 20.0 / cooldown : 4.0;

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(" ")
                .append(Component.translatable("attribute.modifier.equals.0",
                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage),
                        Component.translatable("attribute.name.generic.attack_damage")))
                .withStyle(ChatFormatting.DARK_GREEN));

        tooltip.add(Component.literal(" ")
                .append(Component.translatable("attribute.modifier.equals.0",
                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(speed),
                        Component.translatable("attribute.name.generic.attack_speed")))
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    public static void addAmmoType(List<Component> tooltip, WeaponData data) {
        var descriptionId = WeaponStateHelper.getCurrentAmmo(data).getDescriptionId();

        tooltip.add(Component.literal(" ")
                .append(Component.translatable("info.ntgl.ammo_type",
                        Component.translatable(descriptionId).withStyle(ChatFormatting.DARK_GREEN))
                .withStyle(ChatFormatting.GRAY)));
    }

    public static void addFireRate(List<Component> tooltip, WeaponData data) {
        var rate = WeaponModifierHelper.getRate(data);
        rate = rate == 0 ? 0 : 20 / rate;

        tooltip.add(Component.literal(" ")
                .append(Component.translatable("info.ntgl.rate",
                        Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rate)).withStyle(ChatFormatting.DARK_GREEN))
                .withStyle(ChatFormatting.GRAY)));
    }

    public static void addFuel(List<Component> tooltip, WeaponData weaponData) {
        var allFuel = WeaponModifierHelper.getAllFuel(weaponData);
        for (var fuelType : allFuel) {
            var fuelAmount = FuelUtils.getFuel(weaponData.weapon, fuelType);
            var maxFuel = WeaponModifierHelper.getMaxFuel(fuelType.getId(), weaponData);

            tooltip.add(Component.literal(" ")
                    .append(Component.translatable(fuelType.getDescriptionId()).withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(": " + fuelAmount + "/" + maxFuel).withStyle(ChatFormatting.DARK_GREEN)));
        }
    }

    public static void addAmmo(List<Component> tooltip, CompoundTag tagCompound, WeaponData weaponData) {
        if (tagCompound.getBoolean("IgnoreAmmo")) {
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.ignore_ammo"))
                    .withStyle(ChatFormatting.DARK_GREEN));
        } else {
            int ammoCount = WeaponStateHelper.getAmmoCount(weaponData);
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable("info.ntgl.ammo",
                            Component.literal(ammoCount + "/" + WeaponModifierHelper.getMaxAmmo(weaponData)).withStyle(ChatFormatting.DARK_GREEN))
                    .withStyle(ChatFormatting.GRAY)));
        }
    }

    public static void addDamage(List<Component> tooltip, WeaponData weaponData) {
        var damage = WeaponStateHelper.getProjectileDamage(weaponData);
        tooltip.add(Component.literal(" ")
                .append(Component.translatable("info.ntgl.damage",
                        Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)).withStyle(ChatFormatting.DARK_GREEN))
                .withStyle(ChatFormatting.GRAY)));
    }

    public static void addAttachmentsStats(List<Component> tooltip, ItemStack stack, WeaponData data) {
        var validTypes = WeaponModifierHelper.getAttachmentTypes(data);
        if (validTypes.isEmpty()) return;

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("info.ntgl.header.attachments").withStyle(ChatFormatting.DARK_GRAY));

        boolean isShift = net.minecraft.client.gui.screens.Screen.hasShiftDown();

        for (com.nukateam.ntgl.common.data.holders.AttachmentType type : validTypes) {
            var attachmentItem = WeaponStateHelper.getAttachmentItem(type, stack);
            var typeComponent = type.getTranslationComponent().withStyle(ChatFormatting.GRAY);

            if (!attachmentItem.isEmpty()) {
                tooltip.add(Component.literal(" ")
                        .append(typeComponent)
                        .append(": ")
                        .append(attachmentItem.getHoverName().copy().withStyle(ChatFormatting.DARK_GREEN)));

                if (isShift && attachmentItem.getItem() instanceof com.nukateam.ntgl.common.data.attachment.IAttachment<?> iAttachment) {
                    var props = iAttachment.getProperties();
                    var perks = props.getPerks(attachmentItem);
                    if (perks != null) {
                        for (Component perk : perks) {
                            tooltip.add(Component.literal("   ").append(perk));
                        }
                    }
                }
            } else {
                tooltip.add(Component.literal(" ")
                        .append(typeComponent)
                        .append(": ")
                        .append(Component.translatable("info.ntgl.none").withStyle(ChatFormatting.DARK_GRAY)));
            }
        }
    }
}
