package com.nukateam.chassis_core.common.util.helpers;

import com.nukateam.ntgl.common.foundation.init.NtglComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        newTag.putString(CHASSIS_ENTITY_ID, BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
        NtglComponents.setWeaponTag(stack, newTag);
        target.remove(Entity.RemovalReason.DISCARDED);
        return stack;
    }

    @Nullable
    public static Entity entityFromItem(ItemStack stack, Level world) {
        var tag = NtglComponents.getWeaponTag(stack);
        if (tag != null && !tag.getString(CHASSIS_ENTITY_ID).isEmpty()) {
            var id = tag.getString(CHASSIS_ENTITY_ID);
            var type = EntityType.byString(id).orElse(null);
            if (type != null) {
                Entity entity = type.create(world);

                if (entity == null) return null;

                entity.load(tag.getCompound(ENTITY_TAG));

                if (tag.contains(ENTITY_UUID))
                    entity.setUUID(tag.getUUID(ENTITY_UUID));
                return entity;

            }
        }
        return null;
    }

    @SuppressWarnings("DataFlowIssue")
    public static void clearItemTags(ItemStack stack) {
        var tag = NtglComponents.getWeaponTag(stack);
        tag.remove(CHASSIS_ENTITY_ID);
        tag.remove(ENTITY_TAG);
        tag.remove(ENTITY_UUID);
        NtglComponents.setWeaponTag(stack, tag);

    }
}
