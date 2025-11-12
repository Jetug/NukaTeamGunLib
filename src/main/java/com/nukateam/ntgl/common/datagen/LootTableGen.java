package com.nukateam.ntgl.common.datagen;

import com.google.common.collect.ImmutableSet;

import com.nukateam.ntgl.Ntgl;

import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LootTableGen extends LootTableProvider {
    public LootTableGen(PackOutput output) {
        super(output, Collections.emptySet(), ImmutableList.of(new LootTableProvider.SubProviderEntry(BlockProvider::new, LootContextParamSets.BLOCK)));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext context) {}

    private static class BlockProvider extends BlockLootSubProvider {
        protected BlockProvider() {
            super(ImmutableSet.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            this.dropSelf(ModBlocks.WORKBENCH.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BuiltInRegistries.BLOCK.stream().filter(block -> Ntgl.MOD_ID.equals(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getNamespace())).collect(Collectors.toSet());
        }
    }
}