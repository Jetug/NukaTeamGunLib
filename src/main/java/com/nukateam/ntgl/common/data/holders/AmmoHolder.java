package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;

public class AmmoHolder extends ResourceHolder {
    public static final AmmoHolder BURNABLE = new AmmoHolder("burnable", AmmoHolder::isBurnable);
    public static final AmmoHolder WATER    = new AmmoHolder("water"   , AmmoHolder::isWater);

    private static final Map<ResourceLocation, AmmoHolder> ammoTypeMap = new HashMap<>();

    static {
        registerType(BURNABLE);
        registerType(WATER);
    }

    private Function<ItemStack, Boolean> isAcceptable = (i) -> false;

    public AmmoHolder(ResourceLocation id, Function<ItemStack, Boolean> isAcceptable) {
        super(id);
        this.isAcceptable = isAcceptable;
    }

    private AmmoHolder(String id, Function<ItemStack, Boolean> isAcceptable) {
        super(ResourceLocation.tryBuild(Ntgl.MOD_ID, id));
        this.isAcceptable = isAcceptable;
    }

    private AmmoHolder(ResourceLocation id){
        super(id);
        this.isAcceptable = (stack) -> {
           return Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()), id);
        };
    }

    public boolean isAcceptable(ItemStack ammoStack) {
        return isAcceptable.apply(ammoStack);
    }

    public ResourceLocation getIcon() {
        return ResourceLocation.tryBuild(id.getNamespace(), "textures/hud/ammo_type/" + id.getPath() + ".png");
    }

    public static void registerType(AmmoHolder mode) {
        ammoTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AmmoHolder getType(ResourceLocation id) {
        return ammoTypeMap.getOrDefault(id, new AmmoHolder(id));
    }

    public static AmmoHolder getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }

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
