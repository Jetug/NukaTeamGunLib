package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.client.data.util.Easings;
import com.google.gson.JsonDeserializer;
import com.nukateam.ntgl.common.base.gun.FireMode;
import com.nukateam.ntgl.common.base.gun.GripType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

/**
 * Author: MrCrayfish
 */
public class JsonDeserializers {
    public static final JsonDeserializer<ItemStack> ITEM_STACK = (json, typeOfT, context) -> CraftingHelper.getItemStack(json.getAsJsonObject(), true);
    public static final JsonDeserializer<FireMode> FIRE_MODE = (json, typeOfT, context) -> FireMode.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<ResourceLocation> RESOURCE_LOCATION = (json, typeOfT, context) -> new ResourceLocation(json.getAsString());
    public static final JsonDeserializer<GripType> GRIP_TYPE = (json, typeOfT, context) -> GripType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<Easings> EASING = (json, typeOfT, context) -> Easings.byName(json.getAsString());
}
