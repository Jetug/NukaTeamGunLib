package com.nukateam.gunscore.common.foundation.crafting;

import com.nukateam.gunscore.common.data.util.InventoryUtil;
import com.nukateam.gunscore.common.foundation.blockentity.WorkbenchBlockEntity;
import com.nukateam.gunscore.common.foundation.init.ModRecipeSerializers;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class WorkbenchRecipe implements Recipe<WorkbenchBlockEntity> {
    private final ResourceLocation id;
    private final ItemStack item;
    private final ImmutableList<WorkbenchIngredient> materials;

    public WorkbenchRecipe(ResourceLocation id, ItemStack item, ImmutableList<WorkbenchIngredient> materials) {
        this.id = id;
        this.item = item;
        this.materials = materials;
    }

    public ItemStack getItem() {
        return this.item.copy();
    }

    public ImmutableList<WorkbenchIngredient> getMaterials() {
        return this.materials;
    }

    @Override
    public boolean matches(WorkbenchBlockEntity inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(WorkbenchBlockEntity inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return this.item.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.WORKBENCH.get();
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return ModRecipeType.WORKBENCH;
    }

    public boolean hasMaterials(Player player) {
        for (WorkbenchIngredient ingredient : this.getMaterials()) {
            if (!InventoryUtil.hasWorkstationIngredient(player, ingredient)) {
                return false;
            }
        }
        return true;
    }

    public void consumeMaterials(Player player) {
        for (WorkbenchIngredient ingredient : this.getMaterials()) {
            InventoryUtil.removeWorkstationIngredient(player, ingredient);
        }
    }
}
