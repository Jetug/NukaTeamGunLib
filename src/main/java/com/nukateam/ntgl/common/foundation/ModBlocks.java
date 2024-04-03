package com.nukateam.ntgl.common.foundation;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.common.foundation.block.WorkbenchBlock;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Ntgl.MOD_ID);

    public static final RegistryObject<Block> WORKBENCH = registerBlock("workbench",
            () -> new WorkbenchBlock(Block.Properties.of(Material.METAL).strength(1.5F)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModGuns.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()/*.tab(ModItemTabs.WEAPONS)*/));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
