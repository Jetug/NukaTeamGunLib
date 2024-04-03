package com.nukateam.ntgl.common.foundation;

import com.nukateam.ntgl.GunMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> FRAGILE = tag("fragile");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(GunMod.MOD_ID, name));
        }
    }
}
