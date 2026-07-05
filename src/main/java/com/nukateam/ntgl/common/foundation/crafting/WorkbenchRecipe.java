package com.nukateam.ntgl.common.foundation.crafting;

import com.google.common.collect.ImmutableList;
import com.nukateam.ntgl.common.foundation.blockentity.WorkbenchBlockEntity;
import com.nukateam.ntgl.common.foundation.init.ModRecipeSerializers;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class WorkbenchRecipe implements Recipe<WorkbenchBlockEntity> {

    private final ItemStack result;
    private final List<WorkbenchIngredient> materials;

    public WorkbenchRecipe(ItemStack result, List<WorkbenchIngredient> materials) {
        this.result = result;
        this.materials = List.copyOf(materials);
    }

    public ItemStack getItem() {
        return result.copy();
    }

    public List<WorkbenchIngredient> getMaterials() {
        return materials;
    }

    @Override
    public boolean matches(WorkbenchBlockEntity container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(WorkbenchBlockEntity container, RegistryAccess access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.WORKBENCH.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeType.WORKBENCH.get();
    }

    public boolean hasMaterials(Player player) {
        for (WorkbenchIngredient ingredient : materials) {
            if (!InventoryUtil.hasWorkstationIngredient(player, ingredient)) {
                return false;
            }
        }
        return true;
    }

    public void consumeMaterials(Player player) {
        for (WorkbenchIngredient ingredient : materials) {
            InventoryUtil.removeWorkstationIngredient(player, ingredient);
        }
    }
}