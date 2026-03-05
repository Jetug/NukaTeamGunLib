package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Ntgl.MOD_ID);

//    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WorkbenchBlockEntity>> WORKBENCH = register("workbench",
//            WorkbenchBlockEntity::new, () -> new Block[]{ ModBlocks.WORKBENCH.get() });

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String id,
                                                                                                           BlockEntityType.BlockEntitySupplier<T> factoryIn,
                                                                                                           Supplier<Block[]> validBlocksSupplier) {
        return REGISTER.register(id, () -> BlockEntityType.Builder.of(factoryIn, validBlocksSupplier.get()).build(null));
    }
}
