package com.nukateam.gunscore.common.foundation.init;

import com.nukateam.gunscore.common.foundation.container.AttachmentContainer;
import com.nukateam.gunscore.common.foundation.container.WorkbenchContainer;
import com.nukateam.gunscore.common.foundation.blockentity.WorkbenchBlockEntity;
import com.nukateam.gunscore.GunMod;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModContainers {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, GunMod.MOD_ID);

    public static final RegistryObject<MenuType<WorkbenchContainer>> WORKBENCH = register("workbench", (IContainerFactory<WorkbenchContainer>) (windowId, playerInventory, data) -> {
        WorkbenchBlockEntity workstation = (WorkbenchBlockEntity) playerInventory.player.level.getBlockEntity(data.readBlockPos());
        return new WorkbenchContainer(windowId, playerInventory, workstation);
    });

    public static final RegistryObject<MenuType<AttachmentContainer>> ATTACHMENTS = register("attachments", AttachmentContainer::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String id, MenuType.MenuSupplier<T> factory) {
        return REGISTER.register(id, () -> new MenuType<>(factory));
    }
}
