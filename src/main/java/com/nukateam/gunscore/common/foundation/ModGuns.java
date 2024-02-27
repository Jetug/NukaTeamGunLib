package com.nukateam.gunscore.common.foundation;

import com.nukateam.example.common.foundation.items.guns.FatmanGun;
import com.nukateam.example.common.foundation.items.guns.MinigunGun;
import com.nukateam.example.common.foundation.items.guns.PistolGun;
import com.nukateam.example.common.foundation.items.guns.ShotGun;
import com.nukateam.example.common.registery.ModItemTabs;
import com.nukateam.gunscore.common.foundation.item.*;
import com.nukateam.gunscore.GunMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModGuns {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GunMod.MOD_ID);

    ///GUNS
    public static final RegistryObject<GunItem> PISTOL10MM = ITEMS.register("pistol10mm", () -> new PistolGun(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<GunItem> PIPE_PISTOL = ITEMS.register("pipepistol", () -> new PistolGun(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<GunItem> CLASSIC10MM = ITEMS.register("classic10mm", () -> new PistolGun(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<GunItem> SCOUT10MM = ITEMS.register("scout10mm", () -> new PistolGun(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    //public static final RegistryObject<GunItem> CLASSIC10MM_ZAP = ITEMS.register("classic10mm_zapaway", () -> new PistolGun(new Item.Properties().tab(ModItemTabs.NUKA_EQUIP)));
    public static final RegistryObject<GunItem> PIPEREVOLVER = ITEMS.register("piperevolver", () -> new PistolGun(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<GunItem> FATMAN = ITEMS.register("fatman", () -> new FatmanGun(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<GunItem> MINIGUN = ITEMS.register("minigun", () -> new MinigunGun(new Item.Properties().tab(ModItemTabs.WEAPONS)));


    public static final RegistryObject<GunItem> POWDERGUN = ITEMS.register("powdergun", () -> new ShotGun(new Item.Properties().tab(ModItemTabs.WEAPONS)));

    public static final RegistryObject<Item> MISSILE = ITEMS.register("missile", () -> new AmmoItem(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> GRENADE = ITEMS.register("grenade", () -> new GrenadeItem(new Item.Properties().tab(ModItemTabs.WEAPONS), 20 * 4));
    public static final RegistryObject<Item> BASEGRENADE = ITEMS.register("baseballgrenade", () -> new BaseGrenadeItem(new Item.Properties().tab(ModItemTabs.WEAPONS), 20 * 4));
    public static final RegistryObject<Item> STUN_GRENADE = ITEMS.register("stun_grenade", () -> new StunGrenadeItem(new Item.Properties().tab(ModItemTabs.WEAPONS), 72000));

    //Rounds
    public static final RegistryObject<Item> ROUND10MM = ITEMS.register("round10mm",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND38 = ITEMS.register("round38",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> STEELBALLS = ITEMS.register("steel_ball",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND45 = ITEMS.register("round45",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND5MM = ITEMS.register("round5mm",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND44 = ITEMS.register("round44",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND50 = ITEMS.register("round50",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND380 = ITEMS.register("round380",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND556 = ITEMS.register("round556",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> SHOTSHELL = ITEMS.register("shotshell",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND127 = ITEMS.register("round127",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> ROUND22 = ITEMS.register("round22",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));
    public static final RegistryObject<Item> MININUKE = ITEMS.register("mini_nuke",
            () -> new Item(new Item.Properties().tab(ModItemTabs.WEAPONS)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
