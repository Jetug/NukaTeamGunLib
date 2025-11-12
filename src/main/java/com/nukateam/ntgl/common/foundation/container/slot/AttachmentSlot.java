package com.nukateam.ntgl.common.foundation.container.slot;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.AttachmentType;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.foundation.container.AttachmentContainer;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.registries.Registries;

import static com.nukateam.ntgl.common.util.util.WeaponModifierHelper.getConfig;

/**
 * Author: MrCrayfish
 */
public class AttachmentSlot extends Slot {
    private AttachmentContainer container;
    private ItemStack weapon;
    private AttachmentType type;
    private Player player;

    public AttachmentSlot(AttachmentContainer container, Container weaponInventory, ItemStack weapon, AttachmentType type, Player player, int index, int x, int y) {
        super(weaponInventory, index, x, y);
        this.container = container;
        this.weapon = weapon;
        this.type = type;
        this.player = player;
    }

    public AttachmentType getType() {
        return this.type;
    }

    @Override
    public boolean isActive() {
        if (!(this.weapon.getItem() instanceof IWeapon item)) {
            return false;
        }

        var config = getConfig(new WeaponData(weapon, player));
        var modifiedGun = item.getModifiedConfig(this.weapon);
        return modifiedGun.canAttachType(this.type);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!(this.weapon.getItem() instanceof IWeapon item)) {
            return false;
        }

        var modifiedGun = item.getModifiedConfig(this.weapon);

        if (stack.getItem() instanceof IAttachment<?> attachment) {
            var attachments = modifiedGun.getModules().getAttachments().get(attachment.getType());
            if(attachments == null)
                return false;

            var id = Registries.ITEM.getKey(stack.getItem());
            var canAttachType = modifiedGun.canAttachType(this.type);
            var isRightType = attachment.getType().equals(this.type);
            var canAttach = attachment.canAttachTo(this.weapon);
            var isItemAllowed = false;

            for (var att : attachments){
                if(att.getItemId().equals(id)) {
                    isItemAllowed = true;
                    break;
                }
            }

            return isRightType && canAttachType && canAttach && isItemAllowed;
        }

        return false;
    }

    @Override
    public void set(ItemStack stack) {
        super.set(stack);
        checkAmmoCount(weapon, player);
    }

    public static void checkAmmoCount(ItemStack stack, LivingEntity entity) {
        var gunData = new WeaponData(stack, entity);
        var maxAmmo = WeaponModifierHelper.getMaxAmmo(gunData  );
        var ammoCount = WeaponStateHelper.getAmmoCount(gunData);
        var diff = ammoCount - maxAmmo;

        if(diff > 0){
            WeaponStateHelper.setAmmo(stack, maxAmmo);
            var ammoHolder = WeaponStateHelper.getCurrentAmmo(gunData);
            if(ammoHolder.canReturnAmmo()) {
                var ammoItem = Registries.ITEM.getValue(ammoHolder.getId());
                var dropStack = new ItemStack(ammoItem, diff);

                if (entity instanceof Player player && !player.addItem(dropStack)) {
                    player.drop(dropStack, false);
                }
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
