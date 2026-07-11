package com.nukateam.ntgl.common.jei;

import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.helpers.render.ModelRenderUtil;
import com.nukateam.ntgl.modules.crafting.recipe.WorkbenchRecipe;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class WorkbenchCategory implements IRecipeCategory<WorkbenchRecipe> {
    public static final ResourceLocation ID = ResourceLocation.tryBuild(Ntgl.MOD_ID, "workbench");
    public static final ResourceLocation BACKGROUND = ResourceLocation.tryBuild(Ntgl.MOD_ID, "textures/gui/workbench.png");
    public static final String TITLE_KEY = Ntgl.MOD_ID + ".category.workbench.title";
    public static final String MATERIALS_KEY = Ntgl.MOD_ID + ".category.workbench.materials";

    private final IDrawableStatic background;
    private final IDrawableStatic window;
    private final IDrawableStatic inventory;
    private final IDrawableStatic dyeSlot;
    private final IDrawable icon;
    private final Component title;

    public WorkbenchCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(162, 124);
        this.window = helper.createDrawable(BACKGROUND, 7, 15, 162, 72);
        this.inventory = helper.createDrawable(BACKGROUND, 7, 101, 162, 36);
        this.dyeSlot = helper.createDrawable(BACKGROUND, 7, 101, 18, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.WORKBENCH.get()));
        this.title = Component.translatable(TITLE_KEY);
    }

    @Override
    public RecipeType<WorkbenchRecipe> getRecipeType() {
        return NtglPlugin.WORKBENCH;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorkbenchRecipe recipe, IFocusGroup focuses) {
        var output = recipe.result();
        for (int i = 0; i < recipe.materials().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, (i % 8) * 18 + 1, 88 + (i / 8) * 18).addIngredients(recipe.materials().get(i).ingredient());
        }
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(output);
    }

    @Override
    public void draw(WorkbenchRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        this.window.draw(graphics, 0, 0);
        this.inventory.draw(graphics, 0, this.window.getHeight() + 2 + 11 + 2);
        this.dyeSlot.draw(graphics, 140, 51);

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable(MATERIALS_KEY),
                0,
                78,
                0xFFFFFFFF
        );

        var output = recipe.result().copy();
        var displayName = output.getHoverName();

        if (output.getCount() > 1) {
            displayName = Component.empty()
                    .append(displayName)
                    .append(Component.literal(" x " + output.getCount())
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }

        int titleX = this.window.getWidth() / 2;

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                displayName,
                titleX,
                5,
                0xFFFFFFFF
        );

        var poseStack = graphics.pose();

        poseStack.pushPose();
        {
            poseStack.translate(81, 40, 100);
            poseStack.scale(40F, 40F, 40F);

            poseStack.mulPose(Axis.XP.rotationDegrees(-5F));

            var partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
            var player = Minecraft.getInstance().player;

            if (player != null) {
                poseStack.mulPose(
                        Axis.YP.rotationDegrees(player.tickCount + partialTick)
                );
            }

            poseStack.scale(-1, -1, -1);

            var model = ModelRenderUtil.getModel(output);
            var buffer = Minecraft.getInstance().renderBuffers().bufferSource();

            Minecraft.getInstance()
                    .getItemRenderer()
                    .render(
                            output,
                            ItemDisplayContext.FIXED,
                            false,
                            poseStack,
                            buffer,
                            0xF000F0,
                            OverlayTexture.NO_OVERLAY,
                            model
                    );

            buffer.endBatch();
        }
        poseStack.popPose();
    }
}
