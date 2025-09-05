package com.nukateam.ntgl.common.foundation;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> FRAGILE = tag("fragile");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.tryBuild(Ntgl.MOD_ID, name));
        }
    }


    public static boolean isFragile(BlockState state) {
        return Config.COMMON.gameplay.griefing.enableGlassBreaking.get() && state.is(ModTags.Blocks.FRAGILE);
    }
}
