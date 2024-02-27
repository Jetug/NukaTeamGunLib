package com.nukateam.gunscore.common.foundation.init;

import com.nukateam.gunscore.common.foundation.effect.IncurableEffect;
import com.nukateam.gunscore.GunMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModEffects {
    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, GunMod.MOD_ID);

    public static final RegistryObject<IncurableEffect> BLINDED = REGISTER.register("blinded", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<IncurableEffect> DEAFENED = REGISTER.register("deafened", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
}
