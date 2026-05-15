package com.nukateam.ntgl.common.foundation.serializers;

import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NtglEntityDataSerializers {
//    public static final StreamCodec<RegistryFriendlyByteBuf, ProjectileConfig> PROJECTILE_STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ProjectileConfig>() {
//        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);
//
//
//
//        public void encode(RegistryFriendlyByteBuf buffer, ProjectileConfig p_320873_) {
//
//            CompoundTag tag = value.serializeNBT(null);
//            buffer.writeNbt(tag);
//
//            if (p_320873_.isEmpty()) {
//                p_320527_.writeVarInt(0);
//            } else {
//                p_320527_.writeVarInt(p_320873_.getCount());
//                ITEM_STREAM_CODEC.encode(p_320527_, p_320873_.getItemHolder());
//                DataComponentPatch.STREAM_CODEC.encode(p_320527_, p_320873_.components.asPatch());
//            }
//        }
//
//        public ProjectileConfig decode(RegistryFriendlyByteBuf p_320491_) {
//
//
//
//            int i = p_320491_.readVarInt();
//            if (i <= 0) {
//                return ItemStack.EMPTY;
//            } else {
//                Holder<Item> holder = ITEM_STREAM_CODEC.decode(p_320491_);
//                DataComponentPatch datacomponentpatch = DataComponentPatch.STREAM_CODEC.decode(p_320491_);
//                return new ItemStack(holder, i, datacomponentpatch);
//            }
//        }
//    };

//    public static final EntityDataSerializer<ProjectileConfig> PROJECTILE_CONFIG =
//            new EntityDataSerializer<>() {
//
//                @Override
//                public StreamCodec<? super RegistryFriendlyByteBuf, ProjectileConfig> codec() {
//                    return null;
//                }
//
//                @Override
//                public ProjectileConfig copy(ProjectileConfig value) {
//                    return value.copy();
//                }
//
//
//                public void write(FriendlyByteBuf buffer, ProjectileConfig value) {
//                    CompoundTag tag = value.serializeNBT(null);
//                    buffer.writeNbt(tag);
//                }
//
//                public ProjectileConfig read(FriendlyByteBuf buffer) {
//                    CompoundTag tag = buffer.readNbt();
//
//                    if(tag == null)
//                        return new ProjectileConfig();
//
//                    return ProjectileConfig.create(tag);
//                }
//            };
}