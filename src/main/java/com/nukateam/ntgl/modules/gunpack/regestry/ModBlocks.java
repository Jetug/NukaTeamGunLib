package com.nukateam.ntgl.modules.gunpack.regestry;

import com.nukateam.ntgl.common.foundation.block.WorkbenchBlock;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Ntgl.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Ntgl.MOD_ID);

    public static final DeferredHolder<Block, WorkbenchBlock> WORKBENCH =
            registerBlock("workbench",
                    () -> new WorkbenchBlock(Block.Properties.of()
                            .strength(1.5F)
                            .sound(SoundType.METAL)
                            .mapColor(MapColor.METAL)));

    private static <T extends Block> DeferredHolder<Block, T> registerBlock(
            String name, Supplier<T> blockSupplier) {
        DeferredHolder<Block, T> holder = BLOCKS.register(name, blockSupplier);
        registerBlockItem(name, holder);
        return holder;
    }

    private static <T extends Block> void registerBlockItem(
            String name, DeferredHolder<Block, T> blockHolder) {
        ITEMS.register(name,
                () -> new BlockItem(blockHolder.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
    }
}
