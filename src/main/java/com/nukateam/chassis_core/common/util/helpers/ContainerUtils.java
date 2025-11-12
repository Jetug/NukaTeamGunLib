package com.nukateam.chassis_core.common.util.helpers;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class ContainerUtils {
    public static Container copyContainer(Container container) {
        Container copiedContainer = new SimpleContainer(container.getContainerSize());

        for (int slot = 0; slot < container.getContainerSize(); slot++)
            copiedContainer.setItem(slot, container.getItem(slot).copy());

        return copiedContainer;
    }

    public static boolean isContainersEqual(Container prevContainer, Container curContainer) {
        if (prevContainer.getContainerSize() != curContainer.getContainerSize()) return false;

        for (int slot = 0; slot < prevContainer.getContainerSize(); slot++) {
            var prevStack = prevContainer.getItem(slot);
            var curStack = curContainer.getItem(slot);
            if (!ItemStack.matches(prevStack, curStack) )
                return false;
        }

        return true;
    }
}
