package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.gun.WeaponConfig;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.event.GunFireEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * Author: MrCrayfish
 */
public class RecoilHandler {
    private static RecoilHandler instance;
    public float lastRandPitch = 0f;
    public float lastRandYaw = 0f;

    public static RecoilHandler get() {
        if (instance == null) {
            instance = new RecoilHandler();
        }
        return instance;
    }

    private Random random = new Random();
    private double gunRecoilNormal;
    private double gunRecoilAngle;
    private float gunRecoilRandom;
    private float cameraRecoil;
    private float progressCameraRecoil;

    private RecoilHandler() {
    }

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event) {
        if (!event.isClient())
            return;

        if (!Config.SERVER.enableCameraRecoil.get())
            return;

        var heldItem = event.getStack();
        var gunItem = (IWeapon) heldItem.getItem();
        var modifiedGun = gunItem.getModifiedConfig(heldItem);
        var data = new WeaponData(heldItem, event.getEntity());
        var recoilModifier = 1.0F - WeaponModifierHelper.getRecoilModifier(data);

        recoilModifier *= this.getAdsRecoilReduction(modifiedGun);
        this.cameraRecoil = modifiedGun.getGeneral().getRecoilAngle() * recoilModifier;
        this.progressCameraRecoil = 0F;
        this.gunRecoilRandom = random.nextFloat();
        this.lastRandPitch = random.nextFloat();
        this.lastRandYaw = random.nextFloat();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || this.cameraRecoil <= 0)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        if (!Config.SERVER.enableCameraRecoil.get())
            return;

        float recoilAmount = this.cameraRecoil * mc.getDeltaFrameTime() * 0.15F;
        float startProgress = this.progressCameraRecoil / this.cameraRecoil;
        float endProgress = (this.progressCameraRecoil + recoilAmount) / this.cameraRecoil;

        float pitch = mc.player.getXRot();
        if (startProgress < 0.2F) {
            mc.player.setXRot(pitch - ((endProgress - startProgress) / 0.2F) * this.cameraRecoil);
        } else {
            mc.player.setXRot(pitch + ((endProgress - startProgress) / 0.8F) * this.cameraRecoil);
        }

        this.progressCameraRecoil += recoilAmount;

        if (this.progressCameraRecoil >= this.cameraRecoil) {
            this.cameraRecoil = 0;
            this.progressCameraRecoil = 0;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderOverlay(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;

        var heldItem = event.getItemStack();
        if (!(heldItem.getItem() instanceof IWeapon weaponItem))
            return;

        var modifiedGun = weaponItem.getModifiedConfig(heldItem);
        var cooldown = ClientShootingHandler.get().getCooldownPercent(Minecraft.getInstance().player, event.getHand());
        var recoilDurationOffset = modifiedGun.getGeneral().getRecoilDurationOffset();

        cooldown = cooldown >= recoilDurationOffset ?
                (cooldown - recoilDurationOffset) / (1.0F - recoilDurationOffset) : 0.0F;
        if (cooldown >= 0.8) {
            float amount = (1.0F - cooldown) / 0.2F;
            this.gunRecoilNormal = 1 - (--amount) * amount * amount * amount;
        } else {
            float amount = (cooldown / 0.8F);
            this.gunRecoilNormal = amount < 0.5 ? 2 * amount * amount : -1 + (4 - 2 * amount) * amount;
        }

        this.gunRecoilAngle = modifiedGun.getGeneral().getRecoilAngle();
    }

    public double getAdsRecoilReduction(WeaponConfig weaponConfig) {
        return 1.0 - weaponConfig.getGeneral().getRecoilAdsReduction() * AimingHandler.get().getNormalisedAdsProgress();
    }

    public double getGunRecoilNormal() {
        return this.gunRecoilNormal;
    }

    public double getGunRecoilAngle() {
        return this.gunRecoilAngle;
    }

    public float getGunRecoilRandom() {
        return this.gunRecoilRandom;
    }
}
