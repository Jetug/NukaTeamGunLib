package com.nukateam.ntgl.common.util.helpers.context;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public record AmmoContext(ItemStack stack, @Nullable Container container) implements IAmmoContext{
    public static final AmmoContext NONE = new AmmoContext(ItemStack.EMPTY, null);

    public void shrink(int amount, AmmoHolder ammoHolder, LivingEntity entity){
        var ammo = ammoHolder.onConsume().apply(stack, amount);
        for (var stack : ammo) {
            if(!addItem(stack)){
                entity.spawnAtLocation(stack);
            }
        }

        var shrinkAmount = (int)Math.ceil((double) amount / (double)ammoHolder.getValue(stack));
        stack.shrink(shrinkAmount);

        if (container != null) {
            container.setChanged();
        }
    }

    private boolean addItem(ItemStack addItem) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if(container.getItem(i).isEmpty()) {
                container.setItem(i, addItem);
                return true;
            }
        }
        return false;
    }
}
