package com.nukateam.ntgl.common.datagen;

import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LootTableGen extends LootTableProvider {
    public LootTableGen(PackOutput output, Set<ResourceKey<LootTable>> requiredTables,
                        List<SubProviderEntry> subProviders,
                        CompletableFuture<HolderLookup.Provider> registries) {
        super(output, requiredTables, subProviders, registries);
    }

    public static LootTableGen create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableGen(output, Set.of(),
                List.of(new LootTableGen.SubProviderEntry(BlockProvider::new, LootContextParamSets.BLOCK)),
                registries);
    }

    private static class BlockProvider extends BlockLootSubProvider {
        private final List<Block> knownBlocks = ImmutableList.of(
//                ModBlocks.WORKBENCH.get()
        );

        public BlockProvider(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            for (Block block : knownBlocks) {
                this.dropSelf(block);
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return knownBlocks;
        }
    }
}