package com.nukateam.ntgl.mixin.common;

import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;"))
    private void beforeChangeDimension(BlockState state, Level worldIn, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (worldIn.dimension() == Level.END && entity instanceof ItemEntity) {
            var stack = ((ItemEntity) entity).getItem();
            if (stack.getItem() instanceof GunItem) {
                var gun = stack.copy();
                gun.getOrCreateTag().putFloat("Scale", 2.0F);
                ((ItemEntity) entity).setItem(gun);
            }
        }
    }
}
