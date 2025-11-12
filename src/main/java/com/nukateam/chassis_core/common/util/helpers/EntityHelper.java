package com.nukateam.chassis_core.common.util.helpers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityHelper {
    public static final String ENTITY_TAG = "EntityTag";
    public static final String CHASSIS_ENTITY_ID = "ItemEntityID";
    public static final String ENTITY_UUID = "EntityUUID";

    public static void giveEntityItemToPlayer(Player player, ItemStack stack, LivingEntity target) {
        if (!player.level().isClientSide) {
            var entityInItem = entityToItem(stack, target);
            player.getInventory().add(entityInItem);
        }
    }

    public static @NotNull ItemStack entityToItem(ItemStack stack, LivingEntity target) {
        var newTag = new CompoundTag();
        var entityTag = new CompoundTag();

        target.save(entityTag);
        newTag.put(ENTITY_TAG, entityTag);
        newTag.putString(CHASSIS_ENTITY_ID, Registries.ENTITY_TYPE.getKey(target.getType()).toString());
        stack.setTag(newTag);
        target.remove(Entity.RemovalReason.DISCARDED);
        return stack;
    }

    @Nullable
    public static Entity entityFromItem(ItemStack stack, Level world) {
        if (stack.getTag() != null && !stack.getTag().getString(CHASSIS_ENTITY_ID).isEmpty()) {
            var id = stack.getTag().getString(CHASSIS_ENTITY_ID);
            var type = EntityType.byString(id).orElse(null);
            if (type != null) {
                Entity entity = type.create(world);

                if (entity == null) return null;

                entity.load(stack.getTag().getCompound(ENTITY_TAG));

                if (stack.getTag().contains(ENTITY_UUID))
                    entity.setUUID(stack.getTag().getUUID(ENTITY_UUID));
                return entity;

            }
        }
        return null;
    }

    @SuppressWarnings("DataFlowIssue")
    public static void clearItemTags(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        tag.remove(CHASSIS_ENTITY_ID);
        tag.remove(ENTITY_TAG);
        tag.remove(ENTITY_UUID);
        stack.setTag(tag);
    }
}
