package com.nukateam.ntgl.client.data.handler;


import com.nukateam.ntgl.client.data.util.PropertyHelper;
import com.nukateam.ntgl.common.base.gun.GripType;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.GunEnchantmentHelper;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.helpers.PlayerReviveHelper;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageAim;
import com.nukateam.ntgl.common.foundation.ModBlocks;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

import static com.nukateam.ntgl.common.data.util.GunModifierHelper.canRenderInOffhand;

/**
 * Author: MrCrayfish
 */
public class AimingHandler {
    private static AimingHandler instance;

    public static AimingHandler get() {
        if (instance == null) {
            instance = new AimingHandler();
        }
        return instance;
    }

    private static final double MAX_AIM_PROGRESS = 5;
    private final AimTracker localTracker = new AimTracker();
    private final Map<Player, AimTracker> aimingMap = new WeakHashMap<>();
    private double normalisedAdsProgress;
    private boolean aiming = false;

    private AimingHandler() {
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Player player = event.player;
        AimTracker tracker = getAimTracker(player);
        if (tracker != null) {
            tracker.handleAiming(player, player.getItemInHand(InteractionHand.MAIN_HAND));
            if (!tracker.isAiming()) {
                this.aimingMap.remove(player);
            }
        }
    }

    @Nullable
    private AimTracker getAimTracker(Player player) {
        var isAiming = true;

        try { isAiming = ModSyncedDataKeys.AIMING.getValue(player); }
        catch (Exception e) {Ntgl.LOGGER.error(e.getMessage(), e);}

        if (isAiming && !this.aimingMap.containsKey(player)) {
            this.aimingMap.put(player, new AimTracker());
        }
        return this.aimingMap.get(player);
    }

    public float getAimProgress(LivingEntity entity, float partialTicks) {
        if(entity instanceof Player player) {
            if (player.isLocalPlayer())
                return (float) this.localTracker.getNormalProgress(partialTicks);

            var tracker = this.getAimTracker(player);

            if (tracker != null)
                return (float) tracker.getNormalProgress(partialTicks);
        }
        return 0F;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        if (this.isAiming()) {
            if (!this.aiming) {
                ModSyncedDataKeys.AIMING.setValue(player, true);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(true));
                this.aiming = true;
            }
        } else if (this.aiming) {
            ModSyncedDataKeys.AIMING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
            this.aiming = false;
        }

        this.localTracker.handleAiming(player, player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    @SubscribeEvent
    public void onFovUpdate(ViewportEvent.ComputeFov event) {
        if (!GunRenderingHandler.get().getUsedConfiguredFov())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getMainHandItem().isEmpty() || mc.options.getCameraType() != CameraType.FIRST_PERSON)
            return;

        ItemStack heldItem = mc.player.getMainHandItem();
        if (!(heldItem.getItem() instanceof GunItem gunItem))
            return;

        if (AimingHandler.get().getNormalisedAdsProgress() == 0)
            return;

        if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(mc.player))
            return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if (modifiedGun.getModules().getZoom() == null)
            return;

        double time = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getFovCurve().apply(this.normalisedAdsProgress);
        float modifier = Gun.getFovModifier(heldItem, modifiedGun);
        modifier = (1.0F - modifier) * (float) time;
        event.setFOV(event.getFOV() - event.getFOV() * modifier);
    }

    @SubscribeEvent
    public void onClientTick(ClientPlayerNetworkEvent.LoggingOut event) {
        this.aimingMap.clear();
    }

    /**
     * Prevents the crosshair from rendering when aiming down sight
     */
    @SubscribeEvent(receiveCanceled = true)
    public void onRenderOverlay(RenderGuiOverlayEvent event) {
        this.normalisedAdsProgress = this.localTracker.getNormalProgress(event.getPartialTick());
    }

    public boolean isZooming() {
        return this.aiming;
    }

    public boolean isAiming() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isSpectator()) return false;
        if (Debug.isForceAim()) return true;
        if (mc.screen != null || PlayerReviveHelper.isBleeding(mc.player)) return false;

        var mainHandItem = mc.player.getMainHandItem();
        var offhandItem = mc.player.getOffhandItem();

        if (!(mainHandItem.getItem() instanceof GunItem))
            return false;

        if(!mainHandItem.isEmpty() && !offhandItem.isEmpty() && canRenderInOffhand(mainHandItem) && canRenderInOffhand(offhandItem))
            return false;

        var gun = ((GunItem) mainHandItem.getItem()).getModifiedGun(mainHandItem);

        if (!gun.canAimDownSight())
            return false;

        if (mc.player.getOffhandItem().getItem() == Items.SHIELD && gun.getGeneral().getGripType() == GripType.ONE_HANDED)
            return false;

        if (!this.localTracker.isAiming() && this.isLookingAtInteractableBlock())
            return false;

        if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(mc.player))
            return false;

        boolean zooming = mc.options.keyUse.isDown();
        if (Ntgl.controllableLoaded) {
            zooming |= ControllerHandler.isAiming();
        }

        return zooming;
    }

    public boolean isLookingAtInteractableBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult != null && mc.level != null) {
            if (mc.hitResult instanceof BlockHitResult result) {
                BlockState state = mc.level.getBlockState(result.getBlockPos());
                Block block = state.getBlock();
                // Forge should add a tag for intractable blocks so modders can know which blocks can be interacted with :)
                return block instanceof EntityBlock || block == Blocks.CRAFTING_TABLE || block == ModBlocks.WORKBENCH.get() || state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS) || state.is(Tags.Blocks.CHESTS) || state.is(Tags.Blocks.FENCE_GATES);
            } else if (mc.hitResult instanceof EntityHitResult result) {
                return result.getEntity() instanceof ItemFrame;
            }
        }
        return false;
    }

    public double getNormalisedAdsProgress() {
        return this.normalisedAdsProgress;
    }

    public class AimTracker {
        private double currentAim;
        private double previousAim;

        private void handleAiming(Player player, ItemStack heldItem) {
            this.previousAim = this.currentAim;
            if (ModSyncedDataKeys.AIMING.getValue(player) || (player.isLocalPlayer() && AimingHandler.this.isAiming())) {
                if (this.currentAim < MAX_AIM_PROGRESS) {
                    double speed = GunEnchantmentHelper.getAimDownSightSpeed(heldItem);
                    speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, speed);
                    this.currentAim += speed;
                    if (this.currentAim > MAX_AIM_PROGRESS) {
                        this.currentAim = (int) MAX_AIM_PROGRESS;
                    }
                }
            } else {
                if (this.currentAim > 0) {
                    double speed = GunEnchantmentHelper.getAimDownSightSpeed(heldItem);
                    speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, speed);
                    this.currentAim -= speed;
                    if (this.currentAim < 0) {
                        this.currentAim = 0;
                    }
                }
            }
        }

        public boolean isAiming() {
            return this.currentAim != 0 || this.previousAim != 0;
        }

        public double getNormalProgress(float partialTicks) {
            return Mth.clamp((this.previousAim + (this.currentAim - this.previousAim) * partialTicks) / MAX_AIM_PROGRESS, 0.0, 1.0);
        }
    }
}
