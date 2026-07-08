package com.nukateam.ntgl.modules.crafting.recipe;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class WorkbenchRecipeInput implements RecipeInput {
    private final Player player;

    public WorkbenchRecipeInput(Player player) {
        this.player = player;
    }

    public Player player() {
        return player;
    }

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }
}