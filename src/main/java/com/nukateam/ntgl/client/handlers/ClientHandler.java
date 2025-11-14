package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.client.registry.*;
import com.nukateam.ntgl.client.util.handler.*;
import com.nukateam.ntgl.client.input.GunButtonBindings;
import com.nukateam.ntgl.client.render.screen.AttachmentScreen;
import com.nukateam.ntgl.client.render.screen.WorkbenchScreen;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.*;
import com.nukateam.ntgl.common.foundation.init.ModContainers;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

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
        NeoForge.EVENT_BUS.register(AimingHandler.get());
        NeoForge.EVENT_BUS.register(CrosshairHandler.get());
        NeoForge.EVENT_BUS.register(GunRenderingHandler.get());
        NeoForge.EVENT_BUS.register(RecoilHandler.get());
        NeoForge.EVENT_BUS.register(ClientReloadHandler.get());
        NeoForge.EVENT_BUS.register(ClientShootingHandler.get());
        NeoForge.EVENT_BUS.register(ClientEquipHandler.get());
        NeoForge.EVENT_BUS.register(SoundHandler.get());
        NeoForge.EVENT_BUS.register(new EntityModelHandler());

        /* Only register controller events if Controllable is loaded otherwise it will crash */
        if (Ntgl.controllableLoaded) {
            ControllerHandler.init();
//            NeoForge.EVENT_BUS.register(new ControllerHandler());
            GunButtonBindings.register();
        }

//        setupRenderLayers();
        registerScreenFactories();
        AnimationRegistry.register();
    }

//    private static void setupRenderLayers() {
//        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WORKBENCH.get(), RenderType.cutout());
//    }

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
    public static void clientTick(ClientTickEvent.Post event) {
        if(inspectionTimerRight == INSPECTION_DURATION - 2) {
            inspectionTimerLeft = INSPECTION_DURATION;
        }

        if (inspectionTimerRight > 0)
            inspectionTimerRight--;
        if (inspectionTimerLeft > 0)
            inspectionTimerLeft--;
    }

    //TODO: port this
//    @SubscribeEvent
//    public static void onScreenInit(ScreenEvent.Init.Post event) {
//        if (event.getScreen() instanceof MouseSettingsScreen screen) {
//            if (mouseOptionsField == null) {
//                mouseOptionsField = ObfuscationReflectionHelper.findField(MouseSettingsScreen.class, "f_96218_");
//                mouseOptionsField.setAccessible(true);
//            }
//            try {
//                var list = (OptionsList) mouseOptionsField.get(screen);
//                list.addSmall(OptionInstances.createSensitivitySlider(), null);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }

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
