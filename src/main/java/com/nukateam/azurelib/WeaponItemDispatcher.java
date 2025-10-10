package com.nukateam.azurelib;

import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Base dispatcher class that can be extended by specific weapon typess
 * to define their own animation commands
 */
public class WeaponItemDispatcher {
    // Common commands that most guns might use
    protected AzCommand createCommand(String controller, String animation, AzPlayBehavior behavior) {
        return AzCommand.create(controller, animation, behavior);
    }

    // Generic method to trigger animations
    public void triggerAnimation(Entity entity, ItemStack itemStack, String controller, String animation, AzPlayBehavior behavior) {
        AzCommand command = createCommand(controller, animation, behavior);
        command.sendForItem(entity, itemStack);
    }

    // Common gun animations - subclasses can override or add more
    public void firing(Entity entity, ItemStack itemStack) {
        triggerAnimation(entity, itemStack, "base_controller", "firing", AzPlayBehaviors.PLAY_ONCE);
    }

    public void reload(Entity entity, ItemStack itemStack) {
        triggerAnimation(entity, itemStack, "base_controller", "reload", AzPlayBehaviors.PLAY_ONCE);
    }

    public void idle(Entity entity, ItemStack itemStack) {
        triggerAnimation(entity, itemStack, "base_controller", "idle", AzPlayBehaviors.LOOP);
    }

    public void inspect(Entity entity, ItemStack itemStack) {
        triggerAnimation(entity, itemStack, "base_controller", "inspect", AzPlayBehaviors.PLAY_ONCE);
    }
}