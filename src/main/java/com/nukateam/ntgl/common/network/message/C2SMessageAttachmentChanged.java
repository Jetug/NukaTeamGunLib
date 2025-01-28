package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.item.attachment.AttachmentItemBase;
import com.nukateam.ntgl.common.util.constants.Tags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

/**
 * Author: Jetug
 */
public class C2SMessageAttachmentChanged extends PlayMessage<C2SMessageAttachmentChanged> {
    private int containerId;
    private ItemStack attachment;
    private ItemStack weapon;

    public C2SMessageAttachmentChanged() {}

    public C2SMessageAttachmentChanged(int containerId, ItemStack attachment, ItemStack weapon) {
        this.containerId = containerId;
        this.attachment = attachment;
        this.weapon = weapon;
    }

    @Override
    public void encode(C2SMessageAttachmentChanged message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.containerId);
        buffer.writeNbt(message.attachment.save(new CompoundTag()));
        buffer.writeNbt(message.weapon.save(new CompoundTag()));
    }

    @Override
    public C2SMessageAttachmentChanged decode(FriendlyByteBuf buffer) {
        return new C2SMessageAttachmentChanged(buffer.readInt(), ItemStack.of(buffer.readNbt()), ItemStack.of(buffer.readNbt()));
    }

    @Override
    public void handle(C2SMessageAttachmentChanged message, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer();
            if(player == null) return;

            var sameContainer = player.containerMenu.containerId == message.containerId;
            var sameWeapon = player.getInventory().contains(message.weapon);
            var isAttachment = message.attachment.getItem() instanceof IAttachment ;

            if (sameContainer && isAttachment && sameWeapon) {
                var weapon = player.getMainHandItem();

//                ServerPlayHandler.handleAttachments(player);
                for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                    var stack = player.getInventory().getItem(i);
                    if (stack.getItem() == message.attachment.getItem()) {
                        var attachmentsTag = new CompoundTag();

                        if (stack.getItem() instanceof IAttachment attachment) {
                            var tagKey = attachment.getType();
                            attachmentsTag.put(tagKey.toString(), stack.save(new CompoundTag()));
                        }

                        var tag = weapon.getOrCreateTag();
                        tag.put(Tags.ATTACHMENTS, attachmentsTag);

                        player.containerMenu.broadcastChanges();
                        return;
                    }
                }

            }
        }));
        supplier.setHandled(true);
    }

    public static ArrayList<ItemStack> findAttachments(Container inventory, AttachmentType type){
        var result = new ArrayList<ItemStack>();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var stack = inventory.getItem(i);
            if (stack.getItem() instanceof IAttachment attachment
                    && attachment.getType() == type) {
                result.add(stack);
            }
        }

        return result;
    }

//    public static ArrayList<ItemStack> findWeapon(){
//        var result = new ArrayList<ItemStack>();
//        for (int i = 0; i < inventory.getContainerSize(); ++i) {
//            var stack = inventory.getItem(i);
//            if (stack.getItem() instanceof IAttachment attachment
//                    && attachment.getType() == type) {
//                result.add(stack);
//            }
//        }
//
//        return result;
//    }
}
