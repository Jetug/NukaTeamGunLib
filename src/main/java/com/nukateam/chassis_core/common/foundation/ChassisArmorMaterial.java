package com.nukateam.chassis_core.common.foundation;

import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public class ChassisArmorMaterial {
    private static final int[] HEALTH_PER_SLOT = new int[]{11, 16, 13, 13};

    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final float toughness;
    private final int enchantmentValue;
    private final Holder<SoundEvent> sound;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    public ChassisArmorMaterial(String name, int durabilityMultiplier, int[] slotProtections,
                                float toughness, int enchantmentValue,
                                Holder<SoundEvent> sound, float knockbackResistance,
                                Supplier<Ingredient> p_40481_) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.slotProtections = slotProtections;
        this.toughness = toughness;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(p_40481_);
    }

    protected int getPartId(ChassisPart part) {
        if (part.equals(ChassisPart.HELMET)) {
            return 0;
        } else if (part.equals(ChassisPart.BODY_ARMOR)) {
            return 1;
        } else if (part.equals(ChassisPart.LEFT_ARM_ARMOR) || part.equals(ChassisPart.RIGHT_ARM_ARMOR)) {
            return 2;
        } else if (part.equals(ChassisPart.LEFT_LEG_ARMOR) || part.equals(ChassisPart.RIGHT_LEG_ARMOR)) {
            return 3;
        } else {
            return 0;
        }
    }

    public int getDurabilityForSlot(ChassisPart bodyPart) {
        return HEALTH_PER_SLOT[getPartId(bodyPart)] * this.durabilityMultiplier;
    }

    public int getDefenseForSlot(ChassisPart bodyPart) {
        return slotProtections[getPartId(bodyPart)];
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public Holder<SoundEvent> getEquipSound() {
        return this.sound;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}