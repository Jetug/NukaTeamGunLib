package com.nukateam.ntgl.common.base.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeHooks;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class FuelType extends ResourceHolder {
    public static final FuelType BURNABLE = new FuelType("burnable", FuelType::isBurnable);
    public static final FuelType WATER    = new FuelType("water"   , FuelType::isWater);

    private static final Map<ResourceLocation, FuelType> ammoTypeMap = new HashMap<>();

    static {
        registerType(BURNABLE);
        registerType(WATER);
    }

    private Function<ItemStack, Boolean> isAcceptable = (i) -> false;

    private FuelType(String id, Function<ItemStack, Boolean> isAcceptable) {
        super(ResourceLocation.tryBuild(Ntgl.MOD_ID, id));
        this.isAcceptable = isAcceptable;
    }

    public FuelType(ResourceLocation id, Function<ItemStack, Boolean> isAcceptable) {
        super(id);
        this.isAcceptable = isAcceptable;
    }

    public boolean isAcceptable(ItemStack ammoStack) {
        return isAcceptable.apply(ammoStack);
    }

    public ResourceLocation getIcon() {
        return ResourceLocation.tryBuild(id.getNamespace(), "textures/hud/ammo_type/" + id.getPath() + ".png");
    }

    public static void registerType(FuelType mode) {
        ammoTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static FuelType getType(ResourceLocation id) {
        return ammoTypeMap.getOrDefault(id, BURNABLE);
    }

    public static FuelType getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }


//    public Component getType(String id) {
//        return Component.tr;
//    }

    public String getDescriptionId() {
        return "info." + id.getNamespace() + "." + id.getPath();
    }

    private static boolean isBurnable(ItemStack ammoStack) {
        var burnTime = ForgeHooks.getBurnTime(ammoStack, null);
        return burnTime > 0;
    }

    private static boolean isWater(ItemStack ammoStack) {
        return ammoStack.getItem() == Items.WATER_BUCKET;
    }
}
