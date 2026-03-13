package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.event.GunFireEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
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
    private final Map<InteractionHand, WeaponData> weaponData = new HashMap<>();

    private RecoilHandler() {
    }

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event) {
        if (!event.isClient())
            return;

        if (!Config.SERVER.enableCameraRecoil.get())
            return;

        var data = event.getGunData();
        weaponData.put(event.getHand(), data);
        var recoilModifier = 1.0F - WeaponModifierHelper.getRecoilModifier(data);

        recoilModifier *= (float) this.getAdsRecoilReduction(data);
        var recoilAngle = WeaponModifierHelper.getRecoilAngle(data);
        this.cameraRecoil = recoilAngle * recoilModifier;
        this.progressCameraRecoil = 0F;
        this.gunRecoilRandom = random.nextFloat();
        this.lastRandPitch = random.nextFloat();
        this.lastRandYaw = random.nextFloat();
    }

    @SubscribeEvent
    public void onRenderTick(ClientTickEvent.Post event) {
        if (this.cameraRecoil <= 0)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        if (!Config.SERVER.enableCameraRecoil.get())
            return;

        float recoilAmount = this.cameraRecoil * mc.getTimer().getGameTimeDeltaPartialTick(true) * 0.15F;
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

        var data = weaponData.getOrDefault(event.getHand(), new WeaponData(heldItem, Minecraft.getInstance().player));
        this.gunRecoilAngle = WeaponModifierHelper.getRecoilAngle(data);
    }

    public double getAdsRecoilReduction(WeaponData data) {
        var recoilAdsReduction = WeaponModifierHelper.getRecoilAdsReduction(data);
        var normalAdsProgress = AimingHandler.get().getNormalisedAdsProgress();
        return 1.0 - recoilAdsReduction * normalAdsProgress;
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
