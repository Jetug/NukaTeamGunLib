package com.nukateam.gunscore.common.foundation.init;

import com.nukateam.gunscore.GunMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GunMod.MOD_ID);

    public static final RegistryObject<SoundEvent> ITEM_PISTOL_FIRE = register("item.pistol.fire");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_SILENCED_FIRE = register("item.pistol.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_ENCHANTED_FIRE = register("item.pistol.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_RELOAD = register("item.pistol.reload");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_COCK = register("item.pistol.cock");
    public static final RegistryObject<SoundEvent> ITEM_SHOTGUN_FIRE = register("item.shotgun.fire");
    public static final RegistryObject<SoundEvent> ITEM_SHOTGUN_SILENCED_FIRE = register("item.shotgun.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_SHOTGUN_ENCHANTED_FIRE = register("item.shotgun.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_SHOTGUN_COCK = register("item.shotgun.cock");
    public static final RegistryObject<SoundEvent> ITEM_RIFLE_FIRE = register("item.rifle.fire");
    public static final RegistryObject<SoundEvent> ITEM_RIFLE_SILENCED_FIRE = register("item.rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_RIFLE_ENCHANTED_FIRE = register("item.rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_RIFLE_COCK = register("item.rifle.cock");
    public static final RegistryObject<SoundEvent> ITEM_ASSAULT_RIFLE_FIRE = register("item.assault_rifle.fire");
    public static final RegistryObject<SoundEvent> ITEM_ASSAULT_RIFLE_SILENCED_FIRE = register("item.assault_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_ASSAULT_RIFLE_ENCHANTED_FIRE = register("item.assault_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_ASSAULT_RIFLE_COCK = register("item.assault_rifle.cock");
    public static final RegistryObject<SoundEvent> ITEM_GRENADE_LAUNCHER_FIRE = register("item.grenade_launcher.fire");
    public static final RegistryObject<SoundEvent> ITEM_BAZOOKA_FIRE = register("item.bazooka.fire");
    public static final RegistryObject<SoundEvent> ITEM_MINI_GUN_FIRE = register("item.mini_gun.fire");
    public static final RegistryObject<SoundEvent> ITEM_MINI_GUN_ENCHANTED_FIRE = register("item.mini_gun.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_MACHINE_PISTOL_FIRE = register("item.machine_pistol.fire");
    public static final RegistryObject<SoundEvent> ITEM_MACHINE_PISTOL_SILENCED_FIRE = register("item.machine_pistol.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_MACHINE_PISTOL_ENCHANTED_FIRE = register("item.machine_pistol.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_HEAVY_RIFLE_FIRE = register("item.heavy_rifle.fire");
    public static final RegistryObject<SoundEvent> ITEM_HEAVY_RIFLE_SILENCED_FIRE = register("item.heavy_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_HEAVY_RIFLE_ENCHANTED_FIRE = register("item.heavy_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_HEAVY_RIFLE_COCK = register("item.heavy_rifle.cock");
    public static final RegistryObject<SoundEvent> ITEM_GRENADE_PIN = register("item.grenade.pin");
    public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_EXPLOSION = register("entity.stun_grenade.explosion");
    public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_RING = register("entity.stun_grenade.ring");
    public static final RegistryObject<SoundEvent> UI_WEAPON_ATTACH = register("ui.weapon.attach");

    public static final RegistryObject<SoundEvent> ITEM_10MM_FIRE = register("item.pistol10mm.fire");
    public static final RegistryObject<SoundEvent> ITEM_10MM_ENCHANTED_FIRE = register("item.pistol10mm.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_10MM_SILENCED_FIRE = register("item.pistol10mm.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_10MM_COCK = register("item.pistol10mm.cock");
    public static final RegistryObject<SoundEvent> ITEM_10MM_RELOAD = register("item.pistol10mm.reload");

    public static final RegistryObject<SoundEvent> ITEM_MINIGUN_FIRE = register("item.minigun.fire");

    public static final RegistryObject<SoundEvent> ITEM_38MM_FIRE = register("item.pistol38mm.fire");
    public static final RegistryObject<SoundEvent> ITEM_38MM_ENCHANTED_FIRE = register("item.pistol38mm.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_38MM_SILENCED_FIRE = register("item.pistol38mm.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_38MM_COCK = register("item.pistol38mm.cock");
    public static final RegistryObject<SoundEvent> ITEM_38MM_RELOAD = register("item.pistol38mm.reload");

    public static final RegistryObject<SoundEvent> ITEM_FATMAN_FIRE = register("item.fatman.fire");
    public static final RegistryObject<SoundEvent> ITEM_FATMAN_RELOAD = register("item.fatman.reload");

    public static final RegistryObject<SoundEvent> ITEM_45MM_FIRE = register("item.pistol45mm.fire");
    public static final RegistryObject<SoundEvent> ITEM_45MM_ENCHANTED_FIRE = register("item.pistol45mm.enchanted_fire");
    public static final RegistryObject<SoundEvent> ITEM_45MM_SILENCED_FIRE = register("item.pistol45mm.silenced_fire");
    public static final RegistryObject<SoundEvent> ITEM_45MM_COCK = register("item.pistol45mm.cock");
    public static final RegistryObject<SoundEvent> ITEM_45MM_RELOAD = register("item.pistol45mm.reload");

    private static RegistryObject<SoundEvent> register(String key) {
        return REGISTER.register(key, () -> new SoundEvent(new ResourceLocation(GunMod.MOD_ID, key)));
    }
}
