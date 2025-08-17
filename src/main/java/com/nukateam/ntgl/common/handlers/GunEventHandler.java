package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.event.*;
import com.nukateam.ntgl.common.event.GunFireEvent;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEventHandler {

    @SubscribeEvent
    public static void attachmentsChanged(AttachmentEvent event) {
        if(event.getGunData().shooter != null && !event.getGunData().shooter.level().isClientSide) {
            var gunData = event.getGunData();
            var allAmmo = GunModifierHelper.getAmmoItems(gunData);
            var currentAmmo = GunStateHelper.getAmmoHolder(gunData);

            if (!allAmmo.contains(currentAmmo)) {
                ServerPlayHandler.unloadGun((ServerPlayer) gunData.shooter, gunData.gun);
            }
        }
    }

    @SubscribeEvent
    public static void preShoot(GunFireEvent.Pre event) {
        var entity = event.getEntity();
        var heldItem = entity.getItemInHand(event.getHand());

        if (heldItem.getItem() instanceof WeaponItem) {
            if(event.getEntity() instanceof Player player && EquipTracker.isEquiping(player, event.getHand())){
                event.setCanceled(true);
            }

            if(isBroken(entity, heldItem)){
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void postShoot(GunFireEvent.Post event) {
        var entity = event.getEntity();
        var level = event.getEntity().level();
        var heldItem = entity.getItemInHand(event.getHand());
        var tag = heldItem.getTag();

        if (heldItem.getItem() instanceof WeaponItem) {
            if (heldItem.isDamageableItem() && tag != null) {
                if (GunStateHelper.hasAmmo(heldItem)) {
                    damageGun(heldItem, level, entity);
                }
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() / 1.5)) {
                    level.playSound(entity, entity.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 1.75F);
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
                GunModifierHelper.getGun(heldItem).playCockSound(shooter);
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

//    public static void ejectCasing(Level level, LivingEntity livingEntity) {
//        var heldItem = livingEntity.getMainHandItem();
//        var gun = ((WeaponItem) heldItem.getItem()).getModifiedGun(heldItem);
//
//        var lookVec = livingEntity.getLookAngle(); //Get the player's look vector
//        var rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
//        var forwardVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();
//
//        double offsetX = rightVec.x * 0.5 + forwardVec.x * 0.5; //Move the particle 0.5 blocks to the right and 0.5 blocks forward
//        double offsetY = livingEntity.getEyeHeight() - 0.4; //Move the particle slightly below the player's head
//        double offsetZ = rightVec.z * 0.5 + forwardVec.z * 0.5; //Move the particle 0.5 blocks to the right and 0.5 blocks forward
//
//        Vec3 particlePos = livingEntity.getPosition(1).add(offsetX, offsetY, offsetZ); //Add the offsets to the player's position
//
//        var pistolAmmoLocation = ModItems.PISTOL_AMMO.getId();
//        var rifleAmmoLocation = ModItems.RIFLE_AMMO.getId();
//        var shotgunShellLocation = ModItems.SHOTGUN_SHELL.getId();
//        var spectreAmmoLocation = ModItems.SPECTRE_AMMO.getId();
//        var projectileLocation = gun.getAmmoCount().getItem();
//
//        SimpleParticleType casingType = ModParticleTypes.CASING_PARTICLE.get();
//
//        if (projectileLocation != null) {
//            if (projectileLocation.equals(pistolAmmoLocation) || projectileLocation.equals(rifleAmmoLocation)) {
//                casingType = ModParticleTypes.CASING_PARTICLE.get();
//            } else if (projectileLocation.equals(shotgunShellLocation)) {
//                casingType = ModParticleTypes.SHELL_PARTICLE.get();
//            } else if (projectileLocation.equals(spectreAmmoLocation)) {
//                casingType = ModParticleTypes.SPECTRE_CASING_PARTICLE.get();
//            }
//        }
//
//        if (level instanceof ServerLevel serverLevel) {
//            serverLevel.sendParticles(casingType,
//                    particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
//            if (livingEntity.getMainHandItem().getItem().toString().matches(ModItems.BLOSSOM_RIFLE.get().toString())) {
//                serverLevel.sendParticles(ParticleTypes.CHERRY_LEAVES,
//                        particlePos.x, particlePos.y, particlePos.z, 1, 0.3, 0.2, 0.3, 0);
//            }
//        }
//    }
}