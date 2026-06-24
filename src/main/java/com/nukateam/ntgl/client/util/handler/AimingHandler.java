package com.nukateam.ntgl.client.util.handler;


import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.nukateam.example.common.registery.ExampleWeapons;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.client.util.helpers.PropertyHelper;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.WeaponAction;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.util.helpers.compatibility.PlayerReviveHelper;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageAim;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
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

/**
 * Author: MrCrayfish
 */
public class AimingHandler {
    private static AimingHandler instance;
    private WeaponData weaponData = new WeaponData(new ItemStack(ExampleWeapons.CLASSIC10MM.get()), null);

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

    private AimingHandler() {}

    public static boolean isAiming(ItemStack gun) {
        var minecraft = Minecraft.getInstance();
        var progress = get().getAimProgress(minecraft.player, minecraft.getFrameTime());
        return gun.getItem() instanceof IWeapon
                && get().isAiming()
                && progress == 1;
    }

    public static boolean isScoping(ItemStack gun) {
        return AimingHandler.isAiming(gun) && WeaponStateHelper.hasScopeOverlay(gun);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        var player = event.player;
        var tracker = getAimTracker(player);

        if (tracker != null) {
            var heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if(heldItem.getItem() instanceof IWeapon){
                var weaponData = new WeaponData(heldItem, player);
                tracker.handleAiming(weaponData);
            }
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
        if (entity instanceof WearableChassis chassis && chassis.getFirstPassenger() instanceof LivingEntity passenger) {
            return getAimProgress(passenger, partialTicks);
        }
        return 1.0F;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        if (Minecraft.getInstance().player == null) {
            return;
        }

        var player = Minecraft.getInstance().player;
        var heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.getItem() instanceof IWeapon) {
            weaponData = new WeaponData(heldItem, player);

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

            this.localTracker.handleAiming(weaponData);
        }
    }

    @SubscribeEvent
    public void onFovUpdate(ViewportEvent.ComputeFov event) {
        if (!WeaponRenderingHandler.get().getUsedConfiguredFov())
            return;

        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getMainHandItem().isEmpty()
                || mc.options.getCameraType() != CameraType.FIRST_PERSON)
            return;

        var heldItem = mc.player.getMainHandItem();
        if (!(heldItem.getItem() instanceof IWeapon))
            return;

        if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(mc.player))
            return;

        var zoom = WeaponModifierHelper.getZoom(weaponData);
        if (zoom == null)
            return;

        float progress = (float) this.localTracker.getNormalProgress((float) event.getPartialTick());

        // УБИРАЕМ return при progress == 0
        // if (progress == 0) return; // <-- УДАЛИТЬ ЭТУ СТРОКУ

        var time = PropertyHelper.getSightAnimations(heldItem).getFovCurve().apply(progress);
        var modifier = WeaponStateHelper.getFovModifier(weaponData);
        modifier = (1.0F - modifier) * (float) time;

        // Применяем модификатор ВСЕГДА, даже если progress = 0
        // При progress = 0, time = 0, значит modifier = 0, FOV не меняется
        event.setFOV(event.getFOV() - event.getFOV() * modifier);

        Ntgl.LOGGER.debug(
                "progress={}, time={}, modifier={}, fov={}",
                progress,
                time,
                modifier,
                event.getFOV()
        );
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

    public WeaponData getWeaponData() {
        return weaponData;
    }

    public boolean isAiming() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isSpectator()) return false;
        if (Debug.isForceAim()) return true;
        if (mc.screen != null || PlayerReviveHelper.isBleeding(mc.player)) return false;

        var mainHandItem = mc.player.getMainHandItem();
        var offhandItem = mc.player.getOffhandItem();

        if (!(mainHandItem.getItem() instanceof IWeapon))
            return false;

        var mainOneHanded = WeaponModifierHelper.isOneHanded(new WeaponData(mainHandItem, mc.player));
        var offOneHanded = WeaponModifierHelper.isOneHanded(new WeaponData(offhandItem, mc.player));

        if(!mainHandItem.isEmpty() && !offhandItem.isEmpty() && mainOneHanded && offOneHanded)
            return false;

        if (mc.player.getOffhandItem().getItem() == Items.SHIELD) {
            if (WeaponModifierHelper.isOneHanded(new WeaponData(mainHandItem, mc.player))) return false;
        }

        if (!this.localTracker.isAiming() && this.isLookingAtInteractableBlock())
            return false;

        if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(mc.player))
            return false;

        if(mainHandItem.getItem() instanceof IWeapon && offhandItem.getItem() instanceof IWeapon) {
            var off =  WeaponModifierHelper.getGripType(new WeaponData(offhandItem, mc.player));
            if(off.isOneHanded()) {
                return false;
            }
            return false;
        }

//        this.weaponData = new WeaponData(mainHandItem, mc.player);

        return isAimKeyDown(weaponData);

//        boolean zooming = mc.options.keyUse.isDown();
//
//        if (Ntgl.controllableLoaded) {
//            zooming |= ControllerHandler.isAiming();
//        }
//
//        return zooming;
    }

    private boolean isAimKeyDown(WeaponData data) {
        var mc = Minecraft.getInstance();
//        var zooming = mc.options.keyUse.isDown();
//
//        if (Ntgl.controllableLoaded) {
//            zooming |= ControllerHandler.isAiming();
//        }

        if(mc.options.keyAttack.isDown()) {
            data.setWeaponMode(WeaponMode.PRIMARY);
            if(isScopeAction(data)) {
                return true;
            }
        }
        if(mc.options.keyUse.isDown()) {
            data.setWeaponMode(WeaponMode.SECONDARY);
            if(isScopeAction(data)) {
                return true;
            }
        }
        if(NtglKeyBinds.KEY_ADD_ATTACK.isDown()) {
            data.setWeaponMode(WeaponMode.ADDITIONAL);
            if(isScopeAction(data)) {
                return true;
            }
        }
        if(NtglKeyBinds.KEY_ALT_ATTACK.isDown()) {
            data.setWeaponMode(WeaponMode.ALTERNATIVE);
            if(isScopeAction(data)) {
                return true;
            }
        }

        return Ntgl.controllableLoaded && ControllerHandler.isAiming();
    }

    private static boolean isScopeAction(WeaponData data) {
        return WeaponModifierHelper.getWeaponAction(data) == WeaponAction.SCOPE;
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
        private double targetAim; // Добавляем целевое значение

        public AimTracker() {
            this.currentAim = 0;
            this.previousAim = 0;
            this.targetAim = 0;
        }

        private void handleAiming(WeaponData weaponData) {
            assert weaponData.weapon != null && weaponData.wielder instanceof Player;
            var heldItem = weaponData.weapon;
            var player = (Player)weaponData.wielder;

            if(!(heldItem.getItem() instanceof IWeapon))
                return;

            this.previousAim = this.currentAim;

            // Определяем целевое значение
            if (ModSyncedDataKeys.AIMING.getValue(player) || (player.isLocalPlayer() && AimingHandler.this.isAiming())) {
                this.targetAim = MAX_AIM_PROGRESS;
            } else {
                this.targetAim = 0;
            }

            // Плавно двигаемся к цели
            var speed = WeaponModifierHelper.getModifiedAimDownSightSpeed(weaponData);
            if (this.currentAim < this.targetAim) {
                this.currentAim += speed;
                if (this.currentAim > this.targetAim) {
                    this.currentAim = this.targetAim;
                }
            } else if (this.currentAim > this.targetAim) {
                this.currentAim -= speed;
                if (this.currentAim < this.targetAim) {
                    this.currentAim = this.targetAim;
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
