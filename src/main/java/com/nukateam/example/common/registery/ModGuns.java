package com.nukateam.example.common.registery;

import com.nukateam.ntgl.common.base.GunModifiers;
import com.nukateam.ntgl.common.foundation.item.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Barrel;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Magazine;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Stock;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.UnderBarrel;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModGuns {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Ntgl.MOD_ID);
    ///GUNS
    public static final RegistryObject<GunItem> PISTOL = registerGun("pistol10mm");
    public static final RegistryObject<GunItem> PIPE_PISTOL = registerGun("pipepistol");
    public static final RegistryObject<GunItem> CLASSIC10MM = registerGun("classic10mm", 10);
    public static final RegistryObject<GunItem> SCOUT10MM = registerGun("scout10mm");
//    public static final RegistryObject<GunItem> CLASSIC10MM_ZAP = registerGun("classic10mm_zapaway", () -> new PistolGun(new Item.Properties().tab(ModItemTabs.NUKA_EQUIP)));
    public static final RegistryObject<GunItem> PIPEREVOLVER = registerGun("piperevolver");
    public static final RegistryObject<GunItem> FATMAN = registerGun("fatman");
    public static final RegistryObject<GunItem> MINIGUN = registerGun("minigun");

    public static final RegistryObject<GunItem> POWDERGUN = registerGun("powdergun");
    public static final RegistryObject<GunItem> SHOTGUN = registerGun("shotgun");
    public static final RegistryObject<GunItem> FLAMER = registerGun("flamer");

//    public static final RegistryObject<Item> MISSILE = ITEMS.register("missile",
//            () -> new AmmoItem(new Item.Properties().tab(ModItemTabs.WEAPONS)));

    public static final RegistryObject<Item> GRENADE = ITEMS.register("grenade",
            () -> new GrenadeItem(new Item.Properties()/*.tab(ModItemTabs.WEAPONS)*/, 20 * 4));

    public static final RegistryObject<Item> STUN_GRENADE = ITEMS.register("stun_grenade",
            () -> new StunGrenadeItem(new Item.Properties()/*.tab(ModItemTabs.WEAPONS)*/, 72000));

    //Rounds
    public static final RegistryObject<Item> ROUND10MM = ITEMS.register("round10mm",
            () -> new AmmoItem(new Item.Properties().durability(100)/*.tab(ModItemTabs.WEAPONS)*/));

    public static final RegistryObject<Item> ROUND38 = registerAmmo("round38");
    public static final RegistryObject<Item> STEELBALLS = registerAmmo("steel_ball");
    public static final RegistryObject<Item> ROUND45 = registerAmmo("round45");
    public static final RegistryObject<Item> ROUND5MM = registerAmmo("round5mm");
    public static final RegistryObject<Item> ROUND44 = registerAmmo("round44");
    public static final RegistryObject<Item> ROUND50 = registerAmmo("round50");
    public static final RegistryObject<Item> ROUND380 = registerAmmo("round380");
    public static final RegistryObject<Item> ROUND556 = registerAmmo("round556");
    public static final RegistryObject<Item> SHOTSHELL = registerAmmo("shotshell");
    public static final RegistryObject<Item> ROUND127 = registerAmmo("round127");
    public static final RegistryObject<Item> ROUND22 = registerAmmo("round22");
    public static final RegistryObject<Item> MININUKE = registerAmmo("mini_nuke");

    /* Scope Attachments */
    public static final RegistryObject<Item> HOLOGRAPHIC_SIGHT = ITEMS.register("holographic_sight",
            () -> new ScopeItem(Attachments.SHORT_SCOPE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COLLIMATOR_SIGHT = ITEMS.register("collimator_sight",
            () -> new ScopeItem(Attachments.SHORT_SCOPE, new Item.Properties().stacksTo(1)));

    /* Barrel Attachments */
    public static final RegistryObject<Item> SILENCER = ITEMS.register("silencer",
            () -> new BarrelItem(Barrel.create(8.0F, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE), new Item.Properties().stacksTo(1)));

    /* Stock Attachments */
    public static final RegistryObject<Item> LIGHT_STOCK = ITEMS.register("light_stock",
            () -> new StockItem(Stock.create(GunModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<Item> TACTICAL_STOCK = ITEMS.register("tactical_stock",
            () -> new StockItem(Stock.create(GunModifiers.STABILISED), new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<Item> WEIGHTED_STOCK = ITEMS.register("weighted_stock",
            () -> new StockItem(Stock.create(GunModifiers.SUPER_STABILISED), new Item.Properties().stacksTo(1)));

    /* Under Barrel Attachments */
    public static final RegistryObject<Item> LIGHT_GRIP = ITEMS.register("light_grip",
            () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.LIGHT_RECOIL), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPECIALISED_GRIP = ITEMS.register("specialised_grip",
            () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1)));

    /* Magazine Attachments*/
    public static final RegistryObject<Item> EXTENDED_MAGAZINE = ITEMS.register("extended_magazine",
            () -> new MagazineItem(Magazine.create(30, GunModifiers.SLOW_ADS), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRUM_MAGAZINE = ITEMS.register("drum_magazine",
            () -> new MagazineItem(Magazine.create(60, GunModifiers.SLOWER_ADS, GunModifiers.EXTENDED_MAG), new Item.Properties().stacksTo(1)));


    public static RegistryObject<GunItem> registerGun(String name) {
        return ITEMS.register(name, () -> new GunItem(new Item.Properties().stacksTo(1)));
    }

    public static RegistryObject<GunItem> registerGun(String name, int durability) {
        return ITEMS.register(name, () -> new GunItem(new Item.Properties().durability(durability)));
    }

    public static RegistryObject<Item> registerAmmo(String name) {
        return ITEMS.register(name, () -> new AmmoItem(new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
