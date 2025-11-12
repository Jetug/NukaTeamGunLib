package com.nukateam.example.common.registery;

import com.nukateam.example.common.modifiers.*;
import com.nukateam.ntgl.common.foundation.item.attachment.*;
import com.nukateam.ntgl.common.data.attachment.impl.*;
import com.nukateam.ntgl.common.foundation.item.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ExampleWeapons {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Ntgl.MOD_ID);
    ///WEAPONS
    public static final DeferredHolder<Item, WeaponItem> PISTOL10MM = registerGun("pistol10mm", new TestModifier());
    public static final DeferredHolder<Item, WeaponItem> PIPE_PISTOL = registerGun("pipepistol");
    public static final DeferredHolder<Item, WeaponItem> CLASSIC10MM = registerGun("classic10mm", 10);
    public static final DeferredHolder<Item, WeaponItem> SCOUT10MM = registerGun("scout10mm");
    public static final DeferredHolder<Item, WeaponItem> PIPE_REVOLVER = registerGun("piperevolver");
    public static final DeferredHolder<Item, WeaponItem> FATMAN = registerGun("fatman");
    public static final DeferredHolder<Item, WeaponItem> MINIGUN = registerGun("minigun", new MinigunModifier());
    public static final DeferredHolder<Item, WeaponItem> POWDERGUN = registerGun("powdergun");
    public static final DeferredHolder<Item, WeaponItem> SHOTGUN = registerGun("shotgun");
    public static final DeferredHolder<Item, WeaponItem> FLAMER = registerGun("flamer");
    public static final DeferredHolder<Item, WeaponItem> GATLING = registerGun("gatling");
    public static final DeferredHolder<Item, WeaponItem> REVOLVER = registerGun("revolver");
    public static final DeferredHolder<Item, WeaponItem> RPG = registerGun("rpg");
    public static final DeferredHolder<Item, WeaponItem> HAMMER = registerGun("hammer");

    public static final DeferredHolder<Item, Item> GRENADE = ITEMS.register("grenade",
            () -> new WeaponItem(new Item.Properties()));

//    public static final DeferredHolder<Item, Item> MISSILE = ITEMS.register("missile",
//            () -> new AmmoItem(new Item.Properties().tab(ModItemTabs.WEAPONS)));

    public static final DeferredHolder<Item, Item> GRENADE_OG = ITEMS.register("grenade_old",
            () -> new WeaponItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> STUN_GRENADE = ITEMS.register("stun_grenade",
            () -> new StunGrenadeItem(new Item.Properties()));

    //Rounds
    public static final DeferredHolder<Item, Item> ROUND10MM = ITEMS.register("round10mm",
            () -> new AmmoItem(new Item.Properties().durability(100)));

    public static final DeferredHolder<Item, Item> ROUND38    = registerAmmo("round38"    );
    public static final DeferredHolder<Item, Item> STEELBALLS = registerAmmo("steel_ball" );
    public static final DeferredHolder<Item, Item> ROUND45    = registerAmmo("round45"    );
    public static final DeferredHolder<Item, Item> ROUND5MM   = registerAmmo("round5mm"   );
    public static final DeferredHolder<Item, Item> ROUND44    = registerAmmo("round44"    );
    public static final DeferredHolder<Item, Item> ROUND50    = registerAmmo("round50"    );
    public static final DeferredHolder<Item, Item> ROUND380   = registerAmmo("round380"   );
    public static final DeferredHolder<Item, Item> ROUND556   = registerAmmo("round556"   );
    public static final DeferredHolder<Item, Item> SHOTSHELL  = registerAmmo("shotshell"  );
    public static final DeferredHolder<Item, Item> ROUND127   = registerAmmo("round127"   );
    public static final DeferredHolder<Item, Item> ROUND22    = registerAmmo("round22"    );
    public static final DeferredHolder<Item, Item> MININUKE   = registerAmmo("mini_nuke"  );
    public static final DeferredHolder<Item, Item> FUEL       = registerAmmo("fuel"  );

    /* Scope Attachments */
    public static final DeferredHolder<Item, Item> HOLOGRAPHIC_SIGHT = ITEMS.register("holographic_sight",
            () -> new ScopeItem(Attachments.LONG_SCOPE, new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> COLLIMATOR_SIGHT = ITEMS.register("collimator_sight",
            () -> new ScopeItem(Attachments.SHORT_SCOPE, new Item.Properties().stacksTo(1)));

    /* Barrel Attachments */
    public static final DeferredHolder<Item, Item> SILENCER = ITEMS.register("silencer",
            () -> new BarrelItem(Barrel.create(8.0F, WeaponModifiers.SILENCED, WeaponModifiers.REDUCED_DAMAGE), new Item.Properties().stacksTo(1)));

    /* Stock Attachments */
    public static final DeferredHolder<Item, Item> LIGHT_STOCK = ITEMS.register("light_stock",
            () -> new StockItem(Stock.create(WeaponModifiers.BETTER_CONTROL), new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> TACTICAL_STOCK = ITEMS.register("tactical_stock",
            () -> new StockItem(Stock.create(WeaponModifiers.STABILISED), new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> WEIGHTED_STOCK = ITEMS.register("weighted_stock",
            () -> new StockItem(Stock.create(WeaponModifiers.SUPER_STABILISED), new Item.Properties().stacksTo(1)));

    /* Under Barrel Attachments */
    public static final DeferredHolder<Item, Item> LIGHT_GRIP = ITEMS.register("light_grip",
            () -> new GripItem(Grip.create(WeaponModifiers.LIGHT_RECOIL), new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SPECIALISED_GRIP = ITEMS.register("specialised_grip",
            () -> new GripItem(Grip.create(WeaponModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1)));

    /* Magazine Attachments*/
    public static final DeferredHolder<Item, Item> EXTENDED_MAGAZINE = ITEMS.register("extended_magazine",
            () -> new MagazineItem(Magazine.create(30, WeaponModifiers.SLOW_ADS), new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> DRUM_MAGAZINE = ITEMS.register("drum_magazine",
            () -> new MagazineItem(Magazine.create(60, WeaponModifiers.SLOWER_ADS, WeaponModifiers.EXTENDED_MAG), new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> HEAD_STONE = ITEMS.register("hammer_stone",
            () -> new GripItem(Grip.create(WeaponModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> HAMMER_DIAMOND = ITEMS.register("hammer_diamond",
            () -> new GripItem(Grip.create(WeaponModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> AMMO_BOX = ITEMS.register("ammo_box", () ->
            new AmmoBoxItem(new Item.Properties().stacksTo(1), 100));

    public static DeferredHolder<Item, WeaponItem> registerGun(String name, IWeaponModifier... modifiers) {
        return ITEMS.register(name, () -> new WeaponItem(new Item.Properties().stacksTo(1), modifiers));
    }

    public static DeferredHolder<Item, WeaponItem> registerGun(String name, int durability) {
        return ITEMS.register(name, () -> new WeaponItem(new Item.Properties().durability(durability)));
    }

    public static DeferredHolder<Item, Item> registerAmmo(String name) {
        return ITEMS.register(name, () -> new AmmoItem(new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
