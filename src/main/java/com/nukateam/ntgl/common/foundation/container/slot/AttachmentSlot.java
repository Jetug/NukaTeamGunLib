package com.nukateam.ntgl.common.foundation.container.slot;

import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.foundation.container.AttachmentContainer;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

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
        if (!(this.weapon.getItem() instanceof GunItem)) {
            return false;
        }
        GunItem item = (GunItem) this.weapon.getItem();
        Gun modifiedGun = item.getModifiedGun(this.weapon);
        if (!(stack.getItem() instanceof IAttachment attachment)) {
            return false;
        }

        var gun = getGun(weapon);
        return attachment.getType() == this.type && modifiedGun.canAttachType(this.type, gun) && attachment.canAttachTo(this.weapon);
    }

    @Override
    public void setChanged() {
        if (this.container.isLoaded()) {
            this.player.level().playSound(null, this.player.getX(), this.player.getY() + 1.0, this.player.getZ(), ModSounds.UI_WEAPON_ATTACH.get(), SoundSource.PLAYERS, 0.5F, this.hasItem() ? 1.0F : 0.75F);
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
