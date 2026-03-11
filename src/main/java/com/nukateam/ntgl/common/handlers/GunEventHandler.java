package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.foundation.init.NtglComponents;
import com.nukateam.ntgl.common.foundation.init.NtglGameEvents;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.event.*;
import com.nukateam.ntgl.common.event.GunFireEvent;

import com.nukateam.ntgl.common.util.util.FuelUtils;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class GunEventHandler {
    @SubscribeEvent
    public static void attachmentsChanged(AttachmentEvent.ContainerUpdateEvent event) {}

    @SubscribeEvent
    public static void preReload(GunReloadEvent.Pre event) {
        if(!event.isClient()){
            PacketHandler.sendAnimation(event.getEntity(), event.getHand(), AnimationType.RELOAD);
        }
    }

    @SubscribeEvent
    public static void preShoot(GunFireEvent.Pre event) {
        var entity = event.getEntity();
        var heldItem = entity.getItemInHand(event.getHand());

        if (heldItem.getItem() instanceof IWeapon) {
            if(event.getEntity() instanceof Player player && EquipTracker.isEquiping(player, event.getHand())){
                event.setCanceled(true);
            }

            if(isBroken(entity, heldItem)){
                event.setCanceled(true);
            }

            if (!FuelUtils.hasFuel(event.getGunData(), false)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void postShoot(GunFireEvent.Post event) {
        var entity = event.getEntity();
        var level = event.getEntity().level();
        var heldItem = entity.getItemInHand(event.getHand());

        if (heldItem.getItem() instanceof IWeapon) {
            if (heldItem.isDamageableItem()) {
                if (WeaponStateHelper.hasAmmo(heldItem)) {
                    damageGun(heldItem, level, entity);
                }
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() / 1.5)) {
                    level.playSound(entity, entity.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 1.75F);
                }
            }

            if(!event.isClient()){
                PacketHandler.sendAnimation(entity, event.getHand(), AnimationType.FIRE);

                if(!WeaponModifierHelper.isSilencedFire(new WeaponData(heldItem, entity))){
                    NtglGameEvents.gunshotEvent(level, entity);
                    level.gameEvent(entity, GameEvent.PROJECTILE_SHOOT, entity.blockPosition());
                }

            }
        }
    }

    private static boolean isBroken(LivingEntity shooter, ItemStack heldItem) {
        var level = shooter.level();

        if (heldItem.isDamageableItem()) {
            int maxDamage = heldItem.getMaxDamage();
            int currentDamage = heldItem.getDamageValue();

            if (currentDamage == (maxDamage - 1)) {
                level.playSound(
                        shooter, shooter.blockPosition(),
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                        1.0F, 1.0F
                );
                return true;
            }

            if (currentDamage == maxDamage) {
                WeaponModifierHelper.getConfig(new WeaponData(heldItem, shooter)).playCockSound(shooter);
                return true;
            }
        }
        return false;
    }

    public static void damageGun(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && player.getAbilities().instabuild)
            return;

        if (stack.isDamageableItem()) {
            int maxDamage = stack.getMaxDamage();
            int currentDamage = stack.getDamageValue();
            if (currentDamage >= (maxDamage - 1)) {
                if (currentDamage >= (maxDamage - 2)) {
                    level.playSound(entity, entity.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            } else {
                stack.hurtAndBreak(1, entity, null);
            }
        }
    }
}