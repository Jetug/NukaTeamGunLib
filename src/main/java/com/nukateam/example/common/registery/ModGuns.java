package com.nukateam.example.common.registery;

import com.nukateam.example.common.modifiers.*;
import com.nukateam.ntgl.common.foundation.item.attachment.*;
import com.nukateam.ntgl.common.data.attachment.impl.*;
import com.nukateam.ntgl.common.foundation.item.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public class ModGuns {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Ntgl.MOD_ID);
    ///GUNS
    public static final RegistryObject<WeaponItem> PISTOL10MM = registerGun("pistol10mm", new TestModifier());
    public static final RegistryObject<WeaponItem> PIPE_PISTOL = registerGun("pipepistol");
    public static final RegistryObject<WeaponItem> CLASSIC10MM = registerGun("classic10mm", 10);
    public static final RegistryObject<WeaponItem> SCOUT10MM = registerGun("scout10mm");
    public static final RegistryObject<WeaponItem> PIPE_REVOLVER = registerGun("piperevolver");
    public static final RegistryObject<WeaponItem> FATMAN = registerGun("fatman");
    public static final RegistryObject<WeaponItem> MINIGUN = registerGun("minigun", new MinigunModifier());
    public static final RegistryObject<WeaponItem> POWDERGUN = registerGun("powdergun");
    public static final RegistryObject<WeaponItem> SHOTGUN = registerGun("shotgun");
    public static final RegistryObject<WeaponItem> FLAMER = registerGun("flamer");
    public static final RegistryObject<WeaponItem> GATLING = registerGun("gatling");
    public static final RegistryObject<WeaponItem> REVOLVER = registerGun("revolver");

    public static final RegistryObject<Item> GRENADE = ITEMS.register("grenade",
            () -> new ThrowableItem(new Item.Properties()));

//    public static final RegistryObject<Item> MISSILE = ITEMS.register("missile",
//            () -> new AmmoItem(new Item.Properties().tab(ModItemTabs.WEAPONS)));

    public static final RegistryObject<Item> GRENADE_OG = ITEMS.register("grenade_old",
            () -> new ThrowableItem(new Item.Properties()));

    public static final RegistryObject<Item> STUN_GRENADE = ITEMS.register("stun_grenade",
            () -> new StunGrenadeItem(new Item.Properties()));

    //Rounds
    public static final RegistryObject<Item> ROUND10MM = ITEMS.register("round10mm",
            () -> new AmmoItem(new Item.Properties().durability(100)));

    public static final RegistryObject<Item> ROUND38    = registerAmmo("round38"    );
    public static final RegistryObject<Item> STEELBALLS = registerAmmo("steel_ball" );
    public static final RegistryObject<Item> ROUND45    = registerAmmo("round45"    );
    public static final RegistryObject<Item> ROUND5MM   = registerAmmo("round5mm"   );
    public static final RegistryObject<Item> ROUND44    = registerAmmo("round44"    );
    public static final RegistryObject<Item> ROUND50    = registerAmmo("round50"    );
    public static final RegistryObject<Item> ROUND380   = registerAmmo("round380"   );
    public static final RegistryObject<Item> ROUND556   = registerAmmo("round556"   );
    public static final RegistryObject<Item> SHOTSHELL  = registerAmmo("shotshell"  );
    public static final RegistryObject<Item> ROUND127   = registerAmmo("round127"   );
    public static final RegistryObject<Item> ROUND22    = registerAmmo("round22"    );
    public static final RegistryObject<Item> MININUKE   = registerAmmo("mini_nuke"  );
    public static final RegistryObject<Item> FUEL       = registerAmmo("fuel"  );

    /* Scope Attachments */
    public static final RegistryObject<Item> HOLOGRAPHIC_SIGHT = ITEMS.register("holographic_sight",
            () -> new ScopeItem(Attachments.LONG_SCOPE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COLLIMATOR_SIGHT = ITEMS.register("collimator_sight",
            () -> new ScopeItem(Attachments.SHORT_SCOPE, new Item.Properties().stacksTo(1)));

    /* Barrel Attachments */
    public static final RegistryObject<Item> SILENCER = ITEMS.register("silencer",
            () -> new BarrelItem(Barrel.create(8.0F, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE), new Item.Properties().stacksTo(1)));

    /* Stock Attachments */
    public static final RegistryObject<Item> LIGHT_STOCK = ITEMS.register("light_stock",
            () -> new StockItem(Stock.create(GunModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TACTICAL_STOCK = ITEMS.register("tactical_stock",
            () -> new StockItem(Stock.create(GunModifiers.STABILISED), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WEIGHTED_STOCK = ITEMS.register("weighted_stock",
            () -> new StockItem(Stock.create(GunModifiers.SUPER_STABILISED), new Item.Properties().stacksTo(1)));

    /* Under Barrel Attachments */
    public static final RegistryObject<Item> LIGHT_GRIP = ITEMS.register("light_grip",
            () -> new GripItem(Grip.create(GunModifiers.LIGHT_RECOIL), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPECIALISED_GRIP = ITEMS.register("specialised_grip",
            () -> new GripItem(Grip.create(GunModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1)));

    /* Magazine Attachments*/
    public static final RegistryObject<Item> EXTENDED_MAGAZINE = ITEMS.register("extended_magazine",
            () -> new MagazineItem(Magazine.create(30, GunModifiers.SLOW_ADS), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRUM_MAGAZINE = ITEMS.register("drum_magazine",
            () -> new MagazineItem(Magazine.create(60, GunModifiers.SLOWER_ADS, GunModifiers.EXTENDED_MAG), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> AMMO_BOX = ITEMS.register("ammo_box", () ->
            new AmmoBoxItem(new Item.Properties().stacksTo(1), 100));

    public static RegistryObject<WeaponItem> registerGun(String name, IGunModifier... modifiers) {
        return ITEMS.register(name, () -> new WeaponItem(new Item.Properties().stacksTo(1), modifiers));
    }

    public static RegistryObject<WeaponItem> registerGun(String name, int durability) {
        return ITEMS.register(name, () -> new WeaponItem(new Item.Properties().durability(durability)));
    }

    public static RegistryObject<Item> registerAmmo(String name) {
        return ITEMS.register(name, () -> new AmmoItem(new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
