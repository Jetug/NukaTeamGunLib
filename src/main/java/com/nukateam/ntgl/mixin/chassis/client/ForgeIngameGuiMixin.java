package com.nukateam.ntgl.mixin.chassis.client;

import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeGui.class)
@OnlyIn(Dist.CLIENT)
public class ForgeIngameGuiMixin extends Gui {
    public ForgeIngameGuiMixin(Minecraft mc) {
        super(mc, mc.getItemRenderer());
    }

    @Inject(method = "renderHealthMount(IILnet/minecraft/client/gui/GuiGraphics;)V", at = @At("HEAD"), cancellable = true, remap = false)
    protected void renderHealthMount(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (PlayerUtils.isLocalWearingChassis()) ci.cancel();
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;setSeed(J)V"))
    public void render(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        IGuiOverlay overlay = (gui, poseStack1, partialTick1, screenWidth, screenHeight) -> {
            var minecraft = Minecraft.getInstance();
            if (PlayerUtils.isLocalWearingChassis() && !minecraft.options.hideGui && gui.shouldDrawSurvivalElements()) {
                gui.setupOverlayRenderState(true, false);
                gui.renderFood(screenWidth, screenHeight, poseStack1);
            }
        };

        overlay.render((ForgeGui) (Gui) this, graphics, partialTick, screenWidth, screenHeight);
    }
//
//    @Final
//    @Shadow(remap = false)
//    public static final IIngameOverlay FOOD_LEVEL_ELEMENT = OverlayRegistry.registerOverlayTop("Food Level", (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
//        var minecraft = Minecraft.getInstance();
//        if (PlayerUtils.isLocalWearingChassis() && !minecraft.options.hideGui && gui.shouldDrawSurvivalElements()) {
//            gui.setupOverlayRenderState(true, false);
//            gui.renderFood(screenWidth, screenHeight, poseStack);
//        }
//    });
}
