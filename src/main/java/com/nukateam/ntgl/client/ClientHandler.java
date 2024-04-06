package com.nukateam.ntgl.client;

import com.nukateam.ntgl.client.data.handler.*;
import com.nukateam.ntgl.client.input.GunButtonBindings;
import com.nukateam.ntgl.client.input.KeyBinds;
import com.nukateam.ntgl.client.screen.AttachmentScreen;
import com.nukateam.ntgl.client.screen.WorkbenchScreen;
import com.nukateam.ntgl.client.data.util.PropertyHelper;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.*;
import com.nukateam.ntgl.common.foundation.init.ModContainers;
import com.nukateam.ntgl.common.foundation.item.IColored;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.Tag;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.nukateam.ntgl.client.data.handler.ShootingHandler.gunCooldown;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    private static Field mouseOptionsField;

    public static void setup() {
        MinecraftForge.EVENT_BUS.register(AimingHandler.get());
        MinecraftForge.EVENT_BUS.register(BulletTrailRenderingHandler.get());
        MinecraftForge.EVENT_BUS.register(CrosshairHandler.get());
        MinecraftForge.EVENT_BUS.register(GunRenderingHandler.get());
        MinecraftForge.EVENT_BUS.register(RecoilHandler.get());
        MinecraftForge.EVENT_BUS.register(ClientReloadHandler.get());
        MinecraftForge.EVENT_BUS.register(ShootingHandler.get());
        MinecraftForge.EVENT_BUS.register(SoundHandler.get());
        MinecraftForge.EVENT_BUS.register(new EntityModelHandler());

        /* Only register controller events if Controllable is loaded otherwise it will crash */
        if (Ntgl.controllableLoaded) {
            MinecraftForge.EVENT_BUS.register(new ControllerHandler());
            GunButtonBindings.register();
        }

        setupRenderLayers();
        registerColors();
        registerModelOverrides();
        registerScreenFactories();
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
                ItemStack renderingWeapon = GunRenderingHandler.get().getRenderingWeapon();
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
        ForgeRegistries.ITEMS.forEach(item ->
        {
            if (item instanceof IColored) {
                Minecraft.getInstance().getItemColors().register(color, item);
            }
        });
    }

    private static void registerModelOverrides() {
        /* Weapons */
//        ModelOverrides.register(ModItems.ASSAULT_RIFLE.get(), new SimpleModel(SpecialModels.ASSAULT_RIFLE::getModel));
//        ModelOverrides.register(ModItems.BAZOOKA.get(), new SimpleModel(SpecialModels.BAZOOKA::getModel));
//        ModelOverrides.register(ModItems.GRENADE_LAUNCHER.get(), new GrenadeLauncherModel());
//        ModelOverrides.register(ModItems.HEAVY_RIFLE.get(), new SimpleModel(SpecialModels.HEAVY_RIFLE::getModel));
//        ModelOverrides.register(ModItems.MACHINE_PISTOL.get(), new SimpleModel(SpecialModels.MACHINE_PISTOL::getModel));
//        ModelOverrides.register(ModItems.MINI_GUN.get(), new MiniGunModel());
//        ModelOverrides.register(ModItems.PISTOL.get(), new SimpleModel(SpecialModels.PISTOL::getModel));
//        ModelOverrides.register(ModItems.RIFLE.get(), new SimpleModel(SpecialModels.RIFLE::getModel));
//        ModelOverrides.register(ModItems.SHOTGUN.get(), new SimpleModel(SpecialModels.SHOTGUN::getModel));
    }

    private static void registerScreenFactories() {
        MenuScreens.register(ModContainers.WORKBENCH.get(), WorkbenchScreen::new);
        MenuScreens.register(ModContainers.ATTACHMENTS.get(), AttachmentScreen::new);
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        var remove = new ArrayList<ItemStack>();

        if(event.phase == TickEvent.Phase.START) {
            for (var item : gunCooldown) {
                var tag =  item.getOrCreateTag();
                var cool = tag.getInt("Cooldown");
                if(cool > 0)
                    tag.putInt("Cooldown", --cool);
                else remove.add(item);
            }
            gunCooldown.removeAll(remove);
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
                OptionsList list = (OptionsList) mouseOptionsField.get(screen);
                //list.addBig(OptionInstance.createBoolean("t", true));
                //list.addSmall(GunOptions.ADS_SENSITIVITY, GunOptions.CROSSHAIR);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

//    @SubscribeEvent
//    public static void onKeyPressed(InputEvent.Key event) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.player != null && mc.screen == null && event.getAction() == GLFW.GLFW_PRESS) {
//            if (KeyBinds.KEY_ATTACHMENTS.isDown()) {
//                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
//            }
//            /*else if(event.getKey() == GLFW.GLFW_KEY_KP_9)
//            {
//                mc.setScreen(new EditorScreen(null, new Debug.Menu()));
//            }*/
//        }
//    }

    public static void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            PropertyHelper.resetCache();
        });
    }

    public static Screen createEditorScreen(IEditorMenu menu) {
        return new EditorScreen(Minecraft.getInstance().screen, menu);
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