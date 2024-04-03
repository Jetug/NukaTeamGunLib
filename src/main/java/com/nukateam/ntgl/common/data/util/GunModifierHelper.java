package com.nukateam.ntgl.common.data.util;

import com.nukateam.ntgl.common.base.gun.GripType;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.interfaces.IGunModifier;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class GunModifierHelper {
    private static final IGunModifier[] EMPTY = {};

    public static boolean isOneHanded(ItemStack itemStack){
        var gunItem = (GunItem)itemStack.getItem();
        return gunItem.getModifiedGun(itemStack).getGeneral().getGripType() != GripType.ONE_HANDED;
    }

    public static boolean isWeaponFull(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        var gun = ((GunItem)stack.getItem()).getModifiedGun(stack);
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(stack, gun);
    }

    public static boolean canRenderInOffhand(Player player){
        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        return canRenderInOffhand(mainHandItem) && canRenderInOffhand(offhandItem);
    }

    public static boolean canRenderInOffhand(ItemStack stack){
        if(stack.getItem() instanceof GunItem gunItem){
            var animation = gunItem.getModifiedGun(stack).getGeneral().getGripType().getHeldAnimation();
            return animation.canRenderOffhandItem();
        }
        return true;
    }

    private static IGunModifier[] getModifiers(ItemStack weapon, IAttachment.Type type) {
        ItemStack stack = Gun.getAttachment(type, weapon);
        if (!stack.isEmpty() && stack.getItem() instanceof IAttachment<?> attachment) {
            return attachment.getProperties().getModifiers();
        }
        return EMPTY;
    }

    public static int getModifiedProjectileLife(ItemStack weapon, int life) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                life = modifier.modifyProjectileLife(life);
            }
        }
        return life;
    }

    public static double getModifiedProjectileGravity(ItemStack weapon, double gravity) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                gravity = modifier.modifyProjectileGravity(gravity);
            }
        }
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                gravity += modifier.additionalProjectileGravity();
            }
        }
        return gravity;
    }

    public static float getModifiedSpread(ItemStack weapon, float spread) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                spread = modifier.modifyProjectileSpread(spread);
            }
        }
        return spread;
    }

    public static double getModifiedProjectileSpeed(ItemStack weapon, double speed) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                speed = modifier.modifyProjectileSpeed(speed);
            }
        }
        return speed;
    }

    public static float getFireSoundVolume(ItemStack weapon) {
        float volume = 1.0F;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                volume = modifier.modifyFireSoundVolume(volume);
            }
        }
        return Mth.clamp(volume, 0.0F, 16.0F);
    }

    @Deprecated(since = "1.3.0", forRemoval = true)
    public static double getMuzzleFlashSize(ItemStack weapon, double size) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                size = modifier.modifyMuzzleFlashSize(size);
            }
        }
        return size;
    }

    public static double getMuzzleFlashScale(ItemStack weapon, double scale) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                scale = modifier.modifyMuzzleFlashScale(scale);
            }
        }
        return scale;
    }

    public static float getKickReduction(ItemStack weapon) {
        float kickReduction = 1.0F;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                kickReduction *= Mth.clamp(modifier.kickModifier(), 0.0F, 1.0F);
            }
        }
        return 1.0F - kickReduction;
    }

    public static float getRecoilModifier(ItemStack weapon) {
        float recoilReduction = 1.0F;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                recoilReduction *= Mth.clamp(modifier.recoilModifier(), 0.0F, 1.0F);
            }
        }
        return 1.0F - recoilReduction;
    }

    public static boolean isSilencedFire(ItemStack weapon) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                if (modifier.silencedFire()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double getModifiedFireSoundRadius(ItemStack weapon, double radius) {
        double minRadius = radius;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                double newRadius = modifier.modifyFireSoundRadius(radius);
                if (newRadius < minRadius) {
                    minRadius = newRadius;
                }
            }
        }
        return Mth.clamp(minRadius, 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(ItemStack weapon) {
        float additionalDamage = 0.0F;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                additionalDamage += modifier.additionalDamage();
            }
        }
        return additionalDamage;
    }

    public static float getModifiedProjectileDamage(ItemStack weapon, float damage) {
        float finalDamage = damage;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                finalDamage = modifier.modifyProjectileDamage(finalDamage);
            }
        }
        return finalDamage;
    }

    public static float getModifiedDamage(ItemStack weapon, Gun modifiedGun, float damage) {
        float finalDamage = damage;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                finalDamage = modifier.modifyProjectileDamage(finalDamage);
            }
        }
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                finalDamage += modifier.additionalDamage();
            }
        }
        return finalDamage;
    }

    public static double getModifiedAimDownSightSpeed(ItemStack weapon, double speed) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                speed = modifier.modifyAimDownSightSpeed(speed);
            }
        }
        return Mth.clamp(speed, 0.01, Double.MAX_VALUE);
    }

    public static int getModifiedRate(ItemStack weapon, int rate) {
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                rate = modifier.modifyFireRate(rate);
            }
        }
        return Mth.clamp(rate, 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(ItemStack weapon) {
        float chance = 0F;
        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for (IGunModifier modifier : modifiers) {
                chance += modifier.criticalChance();
            }
        }
        chance += GunEnchantmentHelper.getPuncturingChance(weapon);
        return Mth.clamp(chance, 0F, 1F);
    }
}
