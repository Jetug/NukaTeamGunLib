package com.nukateam.ntgl.common.foundation.container.slot;

import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.container.AttachmentContainer;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import static com.nukateam.ntgl.common.data.util.GunModifierHelper.getGun;

/**
 * Author: MrCrayfish
 */
public class AttachmentSlot extends Slot {
    private AttachmentContainer container;
    private ItemStack weapon;
    private ResourceLocation type;
    private Player player;

    public AttachmentSlot(AttachmentContainer container, Container weaponInventory, ItemStack weapon, ResourceLocation type, Player player, int index, int x, int y) {
        super(weaponInventory, index, x, y);
        this.container = container;
        this.weapon = weapon;
        this.type = type;
        this.player = player;
    }

    public ResourceLocation getType() {
        return this.type;
    }

    @Override
    public boolean isActive() {
        if (!(this.weapon.getItem() instanceof GunItem)) {
            return false;
        }

        var gun = getGun(weapon);
        var item = (GunItem) this.weapon.getItem();
        var modifiedGun = item.getModifiedGun(this.weapon);
        return modifiedGun.canAttachType(this.type, gun);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!(this.weapon.getItem() instanceof GunItem item)) {
            return false;
        }

        var modifiedGun = item.getModifiedGun(this.weapon);

        if (stack.getItem() instanceof IAttachment<?> attachment) {
            var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            var attachments = modifiedGun.getModules().getAttachments().get(attachment.getType());
            var canAttachType = modifiedGun.canAttachType(this.type, modifiedGun);
            var isRightType = attachment.getType().equals(this.type);
            var canAttach = attachment.canAttachTo(this.weapon);
            var isItemAllowed = false;

            for (var att : attachments){
                if(att.getItem().equals(id)) {
                    isItemAllowed = true;
                    break;
                }
            }

            return isRightType && canAttachType && canAttach && isItemAllowed;
        }

        return false;
    }

    @Override
    public void set(ItemStack pStack) {
        super.set(pStack);
        checkAmmoCount(weapon, player);
    }

    public static void checkAmmoCount(ItemStack stack, Entity entity) {
        var maxAmmo = GunModifierHelper.getMaxAmmo(stack);
        var ammoCount = Gun.getAmmo(stack);
        var diff = ammoCount - maxAmmo;

        if(diff > 0){
            Gun.setAmmo(stack, maxAmmo);

            var ammoItem = ForgeRegistries.ITEMS.getValue(GunModifierHelper.getAmmoItem(stack));
            var dropStack = new ItemStack(ammoItem, diff);

            if (entity instanceof Player player && !player.addItem(dropStack)) {
                player.drop(dropStack, false);
            }
        }
    }

    @Override
    public void setChanged() {
        if (this.container.isLoaded()) {
            this.player.level().playSound(null,
                    this.player.getX(),
                    this.player.getY() + 1.0,
                    this.player.getZ(),
                    ModSounds.UI_WEAPON_ATTACH.get(),
                    SoundSource.PLAYERS, 0.5F,
                    this.hasItem() ? 1.0F : 0.75F);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack itemstack = this.getItem();
        return (itemstack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(player);
    }
}
