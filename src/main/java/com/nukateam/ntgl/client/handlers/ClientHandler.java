package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.client.registry.*;
import com.nukateam.ntgl.client.settings.OptionInstances;
import com.nukateam.ntgl.client.util.handler.*;
import com.nukateam.ntgl.client.input.GunButtonBindings;
import com.nukateam.ntgl.client.render.screen.AttachmentScreen;
import com.nukateam.ntgl.client.render.screen.WorkbenchScreen;
import com.nukateam.ntgl.client.util.helpers.PropertyHelper;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.*;
import com.nukateam.ntgl.common.foundation.init.ModContainers;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.Tag;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.registries.Registries;

import java.lang.reflect.Field;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    public static final int INSPECTION_DURATION = 60;
    public static final int INSPECTION_OFFSET = 5;
    private static Field mouseOptionsField;

    public static void setup() {
        MinecraftForge.EVENT_BUS.register(AimingHandler.get());
        MinecraftForge.EVENT_BUS.register(CrosshairHandler.get());
        MinecraftForge.EVENT_BUS.register(GunRenderingHandler.get());
        MinecraftForge.EVENT_BUS.register(RecoilHandler.get());
        MinecraftForge.EVENT_BUS.register(ClientReloadHandler.get());
        MinecraftForge.EVENT_BUS.register(ClientShootingHandler.get());
        MinecraftForge.EVENT_BUS.register(ClientEquipHandler.get());
        MinecraftForge.EVENT_BUS.register(SoundHandler.get());
        MinecraftForge.EVENT_BUS.register(new EntityModelHandler());

        /* Only register controller events if Controllable is loaded otherwise it will crash */
        if (Ntgl.controllableLoaded) {
            ControllerHandler.init();
//            MinecraftForge.EVENT_BUS.register(new ControllerHandler());
            GunButtonBindings.register();
        }

        setupRenderLayers();
        registerColors();
        registerScreenFactories();
        AnimationRegistry.register();
    }

    private static void setupRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WORKBENCH.get(), RenderType.cutout());
    }

    private static void registerColors() {
        ItemColor color = (stack, index) ->
        {
            if (!IColored.isDyeable(stack)) {
                return -1;
            }
            if (index == 0 && stack.hasTag() && stack.getTag().contains("Color", Tag.TAG_INT)) {
                return stack.getTag().getInt("Color");
            }
            if (index == 0 && stack.getItem() instanceof IAttachment) {
                var renderingWeapon = GunRenderingHandler.get().getRenderingWeapon();
                if (renderingWeapon != null) {
                    return Minecraft.getInstance().getItemColors().getColor(renderingWeapon, index);
                }
            }
            if (index == 2) // Reticle colour
            {
                return PropertyHelper.getReticleColor(stack);
            }
            return -1;
        };
        Registries.ITEM.forEach(item ->
        {
            if (item instanceof IColored) {
                Minecraft.getInstance().getItemColors().register(color, item);
            }
        });
    }

    private static void registerScreenFactories() {
        MenuScreens.register(ModContainers.WORKBENCH.get(), WorkbenchScreen::new);
        MenuScreens.register(ModContainers.ATTACHMENTS.get(), AttachmentScreen::new);
    }

    private static int inspectionTimerRight;
    private static int inspectionTimerLeft;

    public static void resetInspectionTimer(){
        inspectionTimerRight = INSPECTION_DURATION;
    }

    public static int getInspectionTicks(InteractionHand arm) {
        return arm == InteractionHand.MAIN_HAND ? inspectionTimerRight : inspectionTimerLeft;
    }

    public static boolean isInspecting() {
        return inspectionTimerRight > 0 || inspectionTimerLeft > 0;
    }

    public static int getMaxInspectionTicks() {
        return INSPECTION_DURATION;
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            if(inspectionTimerRight == INSPECTION_DURATION - 2) {
                inspectionTimerLeft = INSPECTION_DURATION;
            }

            if (inspectionTimerRight > 0)
                inspectionTimerRight--;
            if (inspectionTimerLeft > 0)
                inspectionTimerLeft--;
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof MouseSettingsScreen screen) {
            if (mouseOptionsField == null) {
                mouseOptionsField = ObfuscationReflectionHelper.findField(MouseSettingsScreen.class, "f_96218_");
                mouseOptionsField.setAccessible(true);
            }
            try {
                var list = (OptionsList) mouseOptionsField.get(screen);
                list.addSmall(OptionInstances.createSensitivitySlider(), null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            PropertyHelper.resetCache();
        });
    }

    public static Screen createEditorScreen(IEditorMenu menu) {
        return new EditorScreen(Minecraft.getInstance().screen, menu);
    }

    public static int getInspectionTimerRight() {
        return inspectionTimerRight;
    }

    public static void setInspectionTimerRight(int inspectionTimerRight) {
        ClientHandler.inspectionTimerRight = inspectionTimerRight;
    }

    /* Uncomment for debugging headshot hit boxes */

    /*@SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onRenderLiving(RenderLivingEvent.Post event)
    {
        LivingEntity entity = event.getEntity();
        IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());
        if(headshotBox != null)
        {
            AxisAlignedBB box = headshotBox.getHeadshotBox(entity);
            if(box != null)
            {
                WorldRenderer.drawBoundingBox(event.getMatrixStack(), event.getBuffers().getBuffer(RenderType.getLines()), box, 1.0F, 1.0F, 0.0F, 1.0F);

                AxisAlignedBB boundingBox = entity.getBoundingBox().offset(entity.getPositionVec().inverse());
                boundingBox = boundingBox.grow(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get());
                WorldRenderer.drawBoundingBox(event.getMatrixStack(), event.getBuffers().getBuffer(RenderType.getLines()), boundingBox, 0.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }*/
}
