package com.nukateam.ntgl.client.util.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.geo.interfaces.DynamicGeoItem;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.helpers.PropertyHelper;
import com.nukateam.ntgl.client.util.helpers.render.ModelRenderUtil;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.holders.WeaponAction;
import com.nukateam.ntgl.common.foundation.item.interfaces.INtglItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.event.GunFireEvent;
import com.nukateam.ntgl.common.foundation.init.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

import static com.nukateam.ntgl.client.util.helpers.PropertyHelper.*;

@SuppressWarnings("removal")
public class GunRenderingHandler {
    private static GunRenderingHandler instance;
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation( "textures/gui/icons.png"); // Kinda hacky

    public static GunRenderingHandler get() {
        if (instance == null) {
            instance = new GunRenderingHandler();
        }
        return instance;
    }

    public static final ResourceLocation MUZZLE_FLASH_TEXTURE = ResourceLocation.tryBuild(Ntgl.MOD_ID, "textures/effect/muzzle_flash.png");

    private final Random random = new Random();
    private final Set<Integer> entityIdForMuzzleFlash = new HashSet<>();
    private final Set<Integer> entityIdForDrawnMuzzleFlash = new HashSet<>();
    private final Map<Integer, Float> entityIdToRandomValue = new HashMap<>();

    private int sprintTransition;
    private int prevSprintTransition;
    private int sprintCooldown;
    private float sprintIntensity;

    private float offhandTranslate;
    private float prevOffhandTranslate;

    private Field equippedProgressMainHandField;
    private Field prevEquippedProgressMainHandField;

    private float immersiveRoll;
    private float prevImmersiveRoll;
    private float fallSway;
    private float prevFallSway;

    private boolean usedConfiguredFov = true;

    @Nullable
    private ItemStack renderingWeapon;

    private GunRenderingHandler() {
    }

    @Nullable
    public ItemStack getRenderingWeapon() {
        return this.renderingWeapon;
    }

    public void setUsedConfiguredFov(boolean value) {
        this.usedConfiguredFov = value;
    }

    public boolean getUsedConfiguredFov() {
        return this.usedConfiguredFov;
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        this.updateSprinting();
        this.updateMuzzleFlash();
        this.updateOffhandTranslate();
        this.updateImmersiveCamera();
    }

    private void updateSprinting() {
        this.prevSprintTransition = this.sprintTransition;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isSprinting()
                && !ModSyncedDataKeys.SHOOTING_RIGHT.getValue(mc.player)
                && !ModSyncedDataKeys.RELOADING_RIGHT.getValue(mc.player)
                && !AimingHandler.get().isAiming()
                && this.sprintCooldown == 0) {
            if (this.sprintTransition < 5) {
                this.sprintTransition++;
            }
        } else if (this.sprintTransition > 0) {
            this.sprintTransition--;
        }

        if (this.sprintCooldown > 0) {
            this.sprintCooldown--;
        }
    }

    private void updateMuzzleFlash() {
        this.entityIdForMuzzleFlash.removeAll(this.entityIdForDrawnMuzzleFlash);
        this.entityIdToRandomValue.keySet().removeAll(this.entityIdForDrawnMuzzleFlash);
        this.entityIdForDrawnMuzzleFlash.clear();
        this.entityIdForDrawnMuzzleFlash.addAll(this.entityIdForMuzzleFlash);
    }

    private void updateOffhandTranslate() {
        this.prevOffhandTranslate = this.offhandTranslate;
        var mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        var down = false;
        var heldItem = mc.player.getMainHandItem();

        if (heldItem.getItem() instanceof INtglItem) {
            down = WeaponModifierHelper.getGripType(new WeaponData(heldItem, mc.player))
                    .getHeldAnimation()
                    .canRenderOffhandItem();
        }

        float direction = down ? -0.3F : 0.3F;
        this.offhandTranslate = Mth.clamp(this.offhandTranslate + direction, 0.0F, 1.0F);
    }

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event) {
        if (!event.isClient())
            return;

        this.sprintTransition = 0;
        this.sprintCooldown = 20; //TODO make a config option
    }

    /**
     * Handles calculating the FOV of the first person viewport when aiming with a scope. Changing
     * the FOV allows the user to look through the model of the scope. At a high FOV, the model is
     * very hard to see through, so by lowering the FOV it makes it possible to look through it. This
     * avoids having to render the game twice, which saves a lot of performance.
     */
    @SubscribeEvent
    public void onComputeFov(ViewportEvent.ComputeFov event) {
        // We only want to modify the FOV of the viewport for rendering hand/items in first person
        if (event.usedConfiguredFov())
            return;

        // Test if the gun has a scope
        var player = Objects.requireNonNull(Minecraft.getInstance().player);
        var heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof IWeapon weaponItem))
            return;

        var aimHandler = AimingHandler.get();

        if (WeaponModifierHelper.getWeaponAction(aimHandler.getWeaponData()) != WeaponAction.SCOPE)
            return;

        // Change the FOV of the first person viewport based on the scope and aim progress
        if (AimingHandler.get().getNormalisedAdsProgress() <= 0)
            return;

        // Calculate the time curve
        double time = AimingHandler.get().getNormalisedAdsProgress();
        var sightAnimation = PropertyHelper.getSightAnimations(heldItem);
        time = sightAnimation.getViewportCurve().apply(time);

        // Apply the new FOV
        var newFov = event.getFOV(); // Backwards compatibility
        event.setFOV(Mth.lerp(time, event.getFOV(), newFov));
    }

    @SubscribeEvent
    public void onRenderOverlay(@NotNull RenderHandEvent event) {
        var poseStack = event.getPoseStack();
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var isRight = minecraft.options.mainHand().get() == HumanoidArm.RIGHT ?
                event.getHand() == InteractionHand.MAIN_HAND : event.getHand() == InteractionHand.OFF_HAND;
        var heldItem = event.getItemStack();
        var hand = event.getHand();
        var oppositeHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var oppositeStack = player.getItemInHand(oppositeHand);

        if (hand == InteractionHand.OFF_HAND) {
            if(!WeaponModifierHelper.isOneHanded(new WeaponData(heldItem, player)) || !WeaponModifierHelper.isOneHanded(new WeaponData(oppositeStack, player))){
                event.setCanceled(true);
                return;
            }
        }

        if (heldItem.getItem() instanceof IWeapon || heldItem.getItem() instanceof IThrowable) {
            event.setCanceled(true);

            var overrideModel = ItemStack.EMPTY;
            if (heldItem.getTag() != null) {
                if (heldItem.getTag().contains("Model", Tag.TAG_COMPOUND)) {
                    overrideModel = ItemStack.of(heldItem.getTag().getCompound("Model"));
                }
            }

            var model = minecraft.getItemRenderer().getModel(overrideModel.isEmpty() ? heldItem : overrideModel, player.level(), player, 0);
            var rightHandTranslation = model.getTransforms().firstPersonRightHand.translation;
            var transformType = isRight ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

            poseStack.pushPose();
            {
                int offset = isRight ? 1 : -1;

                if (heldItem.getItem() instanceof IWeapon weaponItem) {
                    var modifiedGun = weaponItem.getModifiedConfig(heldItem);
                    var pos = model.getTransforms().firstPersonRightHand.translation;
                    this.applyIronSightTransforms(event, poseStack, model, isRight, heldItem, modifiedGun);
                    this.applyAimingTransforms(poseStack, heldItem, modifiedGun, pos, offset);
                    this.applySwayTransforms(poseStack, heldItem, player, rightHandTranslation, event.getPartialTick());
                }

                this.applyBobbingTransforms(poseStack, event.getPartialTick());

                /* Applies equip progress animation translations */
                float equipProgress = this.getEquipProgress(event.getPartialTick());
                poseStack.translate(0, equipProgress * -0.6F, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(equipProgress * -50F));

                poseStack.translate(0.15 * offset, -1.0, -1.3);//Jetug


    //          this.applySprintingTransforms(player, heldItem, hand, poseStack, event.getPartialTick());
    //            this.applyRecoilTransforms(poseStack, heldItem, modifiedGun);
    //          this.applyReloadTransforms(poseStack, event.getPartialTick());
                this.applyShieldTransforms(poseStack, player, heldItem, event.getPartialTick());


    //        this.renderFirstPersonArms(event, poseStack, hand, heldItem, modifiedGun, packedLight);
                this.renderWeapon(player, heldItem, transformType, event.getPoseStack(), event.getMultiBufferSource(), getWeaponLghtning(event, player));
            }
            poseStack.popPose();
        }
    }

    private Vec3 getArmTransforms(BakedModel model, InteractionHand hand){
        ItemTransform handModel = hand == InteractionHand.MAIN_HAND ?
                model.getTransforms().firstPersonRightHand :
                model.getTransforms().firstPersonLeftHand;

        float translateX = handModel.translation.x();
        float translateY = handModel.translation.y();
        float translateZ = handModel.translation.z();

        return new Vec3(translateX, translateY, translateZ);
    }

    /* Determines the lighting for the weapon. Weapon will appear bright from muzzle flash or light sources */
    private int getWeaponLghtning(RenderHandEvent event, LocalPlayer player) {
        int blockLight = player.isOnFire() ? 15 : player.level().getBrightness(LightLayer.BLOCK, BlockPos.containing(player.getEyePosition(event.getPartialTick())));
        blockLight += (this.entityIdForMuzzleFlash.contains(player.getId()) ? 3 : 0);
        blockLight = Math.min(blockLight, 15);
        int packedLight = LightTexture.pack(blockLight, player.level().getBrightness(LightLayer.SKY, BlockPos.containing(player.getEyePosition(event.getPartialTick()))));

        return packedLight;
    }

//    private void renderFirstPersonArms(RenderHandEvent event, PoseStack poseStack, HumanoidArm hand, ItemStack heldItem, Gun modifiedGun, int packedLight) {
//        poseStack.pushPose();
//        modifiedGun.getGeneral().getGripType().getHeldAnimation().renderFirstPersonArms(
//                Minecraft.getInstance().player, hand,
//                heldItem, poseStack, event.getMultiBufferSource(),
//                packedLight, event.getPartialTick());
//        poseStack.popPose();
//    }

    private void applyIronSightTransforms(RenderHandEvent event, PoseStack poseStack, BakedModel model,
                                          boolean isRight, ItemStack heldItem, WeaponConfig modifiedWeaponConfig) {
        var scaleX = model.getTransforms().firstPersonRightHand.scale.x();
        var scaleY = model.getTransforms().firstPersonRightHand.scale.y();
        var scaleZ = model.getTransforms().firstPersonRightHand.scale.z();
        var translateX = model.getTransforms().firstPersonRightHand.translation.x();
        var translateY = model.getTransforms().firstPersonRightHand.translation.y();
        var translateZ = model.getTransforms().firstPersonRightHand.translation.z();

        if (AimingHandler.get().getNormalisedAdsProgress() > 0) {
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                double xOffset = translateX;
                double yOffset = translateY;
                double zOffset = translateZ;

                /* Offset since rendering translates to the center of the model */
                xOffset -= 0.5 * scaleX;
                yOffset -= 0.5 * scaleY;
                zOffset -= 0.5 * scaleZ;

                /* Translate to the origin of the weapon */
                var gunOrigin = GUN_DEFAULT_ORIGIN;
                xOffset += gunOrigin.x * 0.0625 * scaleX;
                yOffset += gunOrigin.y * 0.0625 * scaleY;
                zOffset += gunOrigin.z * 0.0625 * scaleZ;

                /* Translate to iron sight */
                var ironSightCamera = getIronSightCamera(heldItem, modifiedWeaponConfig).subtract(gunOrigin);
                xOffset += ironSightCamera.x * 0.0625 * scaleX;
                yOffset += ironSightCamera.y * 0.0625 * scaleY;
                zOffset += ironSightCamera.z * 0.0625 * scaleZ;
                zOffset += 0.72;

                /* Controls the direction of the following translations, changes depending on the main hand. */
                var side = isRight ? 1.0F : -1.0F;
                var time = AimingHandler.get().getNormalisedAdsProgress();
                var transition = getSightAnimations(heldItem).getSightCurve().apply(time);

                /* Reverses the original first person translations */
                poseStack.translate(-0.56 * side * transition, 0.52 * transition, 0.72 * transition);

                xOffset += 9.5 * 0.0625;
                yOffset += 7.2 * 0.0625;
                zOffset += 5.5 * 0.0625;

                if(Ntgl.isDebugging()) {
                    xOffset += (double) ClientDebug.X / 10 * 0.0625;
                    yOffset += (double) ClientDebug.Y / 10 * 0.0625;
                    zOffset += (double) ClientDebug.Z / 10 * 0.0625;
                }

                /* Reverses the first person translations of the item in order to position it in the center of the screen */
                poseStack.translate(-xOffset * side * transition, -yOffset * transition, -zOffset * transition);
//                poseStack.translate(0, -8.5 / 16D, 0.5 / 16D);

            }
        }
    }

    private void applyBobbingTransforms(PoseStack poseStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.bobView().get() && mc.getCameraEntity() instanceof Player player) {
            float deltaDistanceWalked = player.walkDist - player.walkDistO;
            float distanceWalked = -(player.walkDist + deltaDistanceWalked * partialTicks);
            float bobbing = Mth.lerp(partialTicks, player.oBob, player.bob);

            /* Reverses the original bobbing rotations and translations so it can be controlled */
            poseStack.mulPose(Axis.XP.rotationDegrees(-(Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F)));
            poseStack.translate(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 0.5F), -(-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * bobbing)), 0.0D);

            /* Slows down the bob by half */
            bobbing *= player.isSprinting() ? 8.0 : 4.0;
            bobbing *= Config.CLIENT.display.bobbingIntensity.get();

            /* The new controlled bobbing */
            double invertZoomProgress = 1.0 - AimingHandler.get().getNormalisedAdsProgress() * this.sprintIntensity;
            //poseStack.translate((double) (Mth.sin(distanceWalked * (float) Math.PI) * cameraYaw * 0.5F) * invertZoomProgress, (double) (-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * cameraYaw)) * invertZoomProgress, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees((Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F) * (float) invertZoomProgress));
            poseStack.mulPose(Axis.XP.rotationDegrees((Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F) * (float) invertZoomProgress));
        }
    }


    private void applyAimingTransforms(PoseStack poseStack, ItemStack heldItem, WeaponConfig modifiedWeaponConfig, Vector3f pos, int offset) {
//        if (!Config.CLIENT.display.oldAnimations.get()) {
        var x = pos.x();
        var y = pos.y();
        var z = pos.z();
        poseStack.translate(x * offset, y, z);
        poseStack.translate(0, -0.25, 0.25);
        var aiming = (float) Math.sin(Math.toRadians(AimingHandler.get().getNormalisedAdsProgress() * 180F));
        aiming = getSightAnimations(heldItem).getAimTransformCurve().apply(aiming);
        poseStack.mulPose(Axis.ZP.rotationDegrees(aiming * 10F * offset));
        poseStack.mulPose(Axis.XP.rotationDegrees(aiming * 5F));
        poseStack.mulPose(Axis.YP.rotationDegrees(aiming * 5F * offset));
        poseStack.translate(0, 0.25, -0.25);
        poseStack.translate(-x * offset, -y, -z);
//        }
    }

    private void applySwayTransforms(PoseStack poseStack, ItemStack heldItem, LocalPlayer player, Vector3f translation, float partialTicks) {
        if (Config.CLIENT.display.weaponSway.get() && player != null) {
            poseStack.translate(translation.x(), translation.y(), translation.z());

            double zOffset = WeaponModifierHelper.getGripType(new WeaponData(heldItem, player)).getHeldAnimation().getFallSwayZOffset();
            poseStack.translate(0, -0.25, zOffset);
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, this.prevFallSway, this.fallSway)));
            poseStack.translate(0, 0.25, -zOffset);

            float bobPitch = Mth.rotLerp(partialTicks, player.xBobO, player.xBob);
            float headPitch = Mth.rotLerp(partialTicks, player.xRotO, player.getXRot());
            float swayPitch = headPitch - bobPitch;
            swayPitch *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
            poseStack.mulPose(Config.CLIENT.display.swayType.get().getPitchRotation().rotationDegrees(swayPitch * Config.CLIENT.display.swaySensitivity.get().floatValue()));

            float bobYaw = Mth.rotLerp(partialTicks, player.yBobO, player.yBob);
            float headYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
            float swayYaw = headYaw - bobYaw;
            swayYaw *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
            poseStack.mulPose(Config.CLIENT.display.swayType.get().getYawRotation().rotationDegrees(swayYaw * Config.CLIENT.display.swaySensitivity.get().floatValue()));

            poseStack.translate(-translation.x(), -translation.y(), -translation.z());
        }
    }

//    private void applySprintingTransforms(Player player, ItemStack stack, HumanoidArm hand, PoseStack poseStack, float partialTicks) {
//        if (Config.CLIENT.display.sprintAnimation.get()
//                && WeaponModifierHelper
//                .getGripType(new GunData(stack, player))
//                .getHeldAnimation()
//                .canApplySprintingAnimation()) {
//            float leftHanded = hand == HumanoidArm.LEFT ? -1 : 1;
//            float transition = (this.prevSprintTransition + (this.sprintTransition - this.prevSprintTransition) * partialTicks) / 5F;
//            transition = (float) Math.sin((transition * Math.PI) / 2);
//            poseStack.translate(-0.25 * leftHanded * transition, -0.1 * transition, 0);
//            poseStack.mulPose(Axis.YP.rotationDegrees(45F * leftHanded * transition));
//            poseStack.mulPose(Axis.XP.rotationDegrees(-25F * transition));
//        }
//    }

    private void applyReloadTransforms(PoseStack poseStack, float partialTicks) {
        float reloadProgress = ClientReloadHandler.get().getReloadProgress(partialTicks);
        poseStack.translate(0, 0.35 * reloadProgress, 0);
        poseStack.translate(0, 0, -0.1 * reloadProgress);
        poseStack.mulPose(Axis.XP.rotationDegrees(45F * reloadProgress));
    }

//    private void applyRecoilTransforms(PoseStack poseStack, Player player, ItemStack item, WeaponConfig weaponConfig) {
//        double recoilNormal = RecoilHandler.get().getGunRecoilNormal();
//        if (WeaponStateHelper.hasAttachmentEquipped(item, AttachmentType.SCOPE)) {
//            recoilNormal -= recoilNormal * (0.5 * AimingHandler.get().getNormalisedAdsProgress());
//        }
//
//        var data = new WeaponData(item, player);
//        var kickReduction = 1.0F - WeaponModifierHelper.getKickReduction(data);
//        var recoilReduction = 1.0F - WeaponModifierHelper.getRecoilModifier(data);
//        var kick = weaponConfig.getGeneral().getRecoilKick() * 0.0625 * recoilNormal * RecoilHandler.get().getAdsRecoilReduction(weaponConfig);
//        var recoilLift = (float) (weaponConfig.getGeneral().getRecoilAngle() * recoilNormal) * (float) RecoilHandler.get().getAdsRecoilReduction(weaponConfig);
//        var recoilSwayAmount = (float) (2F + 1F * (1.0 - AimingHandler.get().getNormalisedAdsProgress()));
//        var recoilSway = (float) ((RecoilHandler.get().getGunRecoilRandom() * recoilSwayAmount - recoilSwayAmount / 2F) * recoilNormal);
//
//        poseStack.translate(0, 0, kick * kickReduction);
//        poseStack.translate(0, 0, 0.15);
//        poseStack.mulPose(Axis.YP.rotationDegrees(recoilSway * recoilReduction));
//        poseStack.mulPose(Axis.ZP.rotationDegrees(recoilSway * recoilReduction));
//        poseStack.mulPose(Axis.XP.rotationDegrees(recoilLift * recoilReduction));
//        poseStack.translate(0, 0, -0.15);
//    }

    private void applyShieldTransforms(PoseStack poseStack, LocalPlayer player, ItemStack stack, float partialTick) {
        if (player.isUsingItem() && player.getOffhandItem().getItem() == Items.SHIELD) {
            if (WeaponModifierHelper.isOneHanded(new WeaponData(stack, player))) {
                double time = Mth.clamp((player.getTicksUsingItem() + partialTick), 0.0, 4.0) / 4.0;
                poseStack.translate(0, 0.35 * time, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(45F * (float) time));
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START))
            return;

        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive())
            return;

        Player player = mc.player;
        if (player == null)
            return;

        if (Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON)
            return;

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.isEmpty())
            return;

//        if (player.isUsingItem()
//                && player.getUsedItemHand() == InteractionHand.MAIN_HAND
//                && heldItem.getItem() instanceof ThrowableItem) {
//            int duration = player.getTicksUsingItem();
//            if (duration >= 10) {
//                float cookTime = 1.0F - ((float) (duration - 10) / (float) (player.getUseItem().getUseDuration() - 10));
//                if (cookTime > 0.0F) {
//                    float scale = 3;
//                    Window window = mc.getWindow();
//                    int i = (int) ((window.getGuiScaledHeight() / 2 - 7 - 60) / scale);
//                    int j = (int) Math.ceil((window.getGuiScaledWidth() / 2 - 8 * scale) / scale);
//
//                    RenderSystem.enableBlend();
//                    RenderSystem.defaultBlendFunc();
//                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
//                    RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
//
//                    GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
//                    graphics.pose().scale(scale, scale, scale);
//                    int progress = (int) Math.ceil((cookTime) * 17.0F) - 1;
//
//                    graphics.blit(GUI_ICONS_LOCATION, j, i, 36, 94, 16, 4, 256, 256);
//                    graphics.blit(GUI_ICONS_LOCATION, j, i, 52, 94, progress, 4, 256, 256);
//
//                    RenderSystem.disableBlend();
//                }
//            }
//        }
    }

    public void applyWeaponScale(ItemStack heldItem, PoseStack stack) {
        if (heldItem.getTag() != null) {
            CompoundTag compound = heldItem.getTag();
            if (compound.contains("Scale", Tag.TAG_FLOAT)) {
                float scale = compound.getFloat("Scale");
                stack.scale(scale, scale, scale);
            }
        }
    }

    public void renderWeapon(@Nullable LivingEntity entity, ItemStack renderStack,
                             ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight) {
        if (renderStack.getItem() instanceof DynamicGeoItem weaponItem) {
            poseStack.pushPose();
            {
                var model = ItemStack.EMPTY;
                if (renderStack.getTag() != null) {
                    if (renderStack.getTag().contains("Model", Tag.TAG_COMPOUND)) {
                        model = ItemStack.of(renderStack.getTag().getCompound("Model"));
                    }
                }

                ModelRenderUtil.applyTransformType(renderStack, poseStack, transformType, entity);

                this.renderingWeapon = renderStack;

                weaponItem.getRenderer().render(
                        entity,
                        model.isEmpty() ? renderStack : model,
                        transformType,
                        poseStack,
                        bufferSource,
                        null,
                        null,
                        packedLight);

                this.renderingWeapon = null;
            }
            poseStack.popPose();
        }
    }

    /**
     * A temporary hack to get the equip progress until Forge fixes the issue.
     * @return
     */
    private float getEquipProgress(float partialTicks) {
        if (this.equippedProgressMainHandField == null) {
            this.equippedProgressMainHandField = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109302_");
            this.equippedProgressMainHandField.setAccessible(true);
        }
        if (this.prevEquippedProgressMainHandField == null) {
            this.prevEquippedProgressMainHandField = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109303_");
            this.prevEquippedProgressMainHandField.setAccessible(true);
        }
        ItemInHandRenderer firstPersonRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        try {
            float equippedProgressMainHand = (float) this.equippedProgressMainHandField.get(firstPersonRenderer);
            float prevEquippedProgressMainHand = (float) this.prevEquippedProgressMainHandField.get(firstPersonRenderer);
            return 1.0F - Mth.lerp(partialTicks, prevEquippedProgressMainHand, equippedProgressMainHand);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0.0F;
    }

    private void updateImmersiveCamera() {
        this.prevImmersiveRoll = this.immersiveRoll;
        this.prevFallSway = this.fallSway;

        var mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        var heldItem = mc.player.getMainHandItem();
        var targetAngle = heldItem.getItem() instanceof IWeapon || !Config.CLIENT.display.restrictCameraRollToWeapons.get() ? mc.player.input.leftImpulse : 0F;
        var speed = mc.player.input.leftImpulse != 0 ? 0.1F : 0.15F;
        this.immersiveRoll = Mth.lerp(speed, this.immersiveRoll, targetAngle);

        var deltaY = (float) Mth.clamp((mc.player.yo - mc.player.getY()), -1.0, 1.0);
        deltaY *= 1.0 - AimingHandler.get().getNormalisedAdsProgress();
        deltaY *= 1.0 - (Mth.abs(mc.player.getXRot()) / 90.0F);
        this.fallSway = Mth.approach(this.fallSway, deltaY * 60F * Config.CLIENT.display.swaySensitivity.get().floatValue(), 10.0F);

        var intensity = mc.player.isSprinting() ? 0.75F : 1.0F;
        this.sprintIntensity = Mth.approach(this.sprintIntensity, intensity, 0.1F);
    }

    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (Config.CLIENT.display.cameraRollEffect.get()
                && this.prevImmersiveRoll != 0
                && this.immersiveRoll != 0
        ){
            float roll = (float) Mth.lerp(event.getPartialTick(), this.prevImmersiveRoll, this.immersiveRoll);
            roll = (float) Math.sin((roll * Math.PI) / 2.0);
            roll *= Config.CLIENT.display.cameraRollAngle.get().floatValue();
            event.setRoll(-roll);
        }
    }
}
