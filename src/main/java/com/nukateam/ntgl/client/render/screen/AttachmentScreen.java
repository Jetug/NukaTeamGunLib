package com.nukateam.ntgl.client.render.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.screen.widget.MiniButton;
import com.nukateam.ntgl.client.util.util.ModelRenderUtil;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.container.AttachmentContainer;
import com.nukateam.ntgl.common.foundation.container.slot.AttachmentSlot;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.util.data.Pos2I;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.nukateam.ntgl.client.util.util.ModelRenderUtil.isMouseWithin;
import static com.nukateam.ntgl.common.util.util.GunModifierHelper.getGunAttachments;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

/**
 * Author: MrCrayfish
 */
public class AttachmentScreen extends AbstractContainerScreen<AttachmentContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("ntgl:textures/gui/attachments.png");
    private static final ResourceLocation SLOT = new ResourceLocation("ntgl:textures/gui/slot.png");
    private static final Component CONFIG_TOOLTIP = translatable("ntgl.button.config.tooltip");
    public static final int SLOT_SIZE = 18;
    public static final int IMAGE_HEIGHT = 214;
    public static final int ATTACHMENT_Y = 107;
    public static final String ATTACHMENT_NOT_APPLICABLE = "slot.ntgl.attachment.not_applicable";
    public static final String ATTACHMENT_INCOMPATIBLE = "slot.ntgl.attachment.incompatible";
    public static final int ATTACHMENT_X = 7;
    public static final int ICON_SIZE = 16;
    public static final String WINDOW_HELP = "container.ntgl.attachments.window_help";

    private final Inventory playerInventory;
    private final Container weaponInventory;

    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;
    private int clickedSlot = -1;

    public AttachmentScreen(AttachmentContainer screenContainer, Inventory playerInventory, Component titleIn) {
        super(screenContainer, playerInventory, titleIn);
        this.playerInventory = playerInventory;
        this.weaponInventory = screenContainer.getWeaponInventory();
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        var buttons = gatherButtons();

        for (int i = 0; i < buttons.size(); i++) {
            var button = buttons.get(i);

            switch (Config.CLIENT.buttonAlignment.get()) {
                case LEFT -> {
                    int titleWidth = minecraft.font.width(title);
                    button.setX(leftPos + titleWidth + 8 + 3 + i * 13);
                }
                case RIGHT -> button.setX(leftPos + imageWidth - 5 - 10 - (buttons.size() - 1 - i) * 13);
            }
            button.setY(topPos + 102 - 5);
            this.addRenderableWidget(button);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.minecraft != null && this.minecraft.player != null) {
            if (!(this.minecraft.player.getMainHandItem().getItem() instanceof GunItem)) {
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY); //Render tool tips

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        var i = 0;
        for (var att : getGunAttachments(getGun()).keySet()) {
            if (isMouseWithinSlot(mouseX, mouseY, left, top, i)) {
                if (!this.menu.getSlot(i).isActive()) {
                    graphics.renderComponentTooltip(this.font, Arrays.asList((translatable(att.getTranslationKey())),
                            translatable(ATTACHMENT_NOT_APPLICABLE)), mouseX, mouseY);
                } else if (this.menu.getSlot(i) instanceof AttachmentSlot slot && slot.getItem().isEmpty()
                        && !this.isCompatible(this.menu.getCarried(), slot)) {
                    graphics.renderComponentTooltip(this.font, Arrays.asList(translatable(ATTACHMENT_INCOMPATIBLE)
                            .withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
                } else if (this.weaponInventory.getItem(i).isEmpty()) {
                    graphics.renderComponentTooltip(this.font, List.of(translatable(att.getTranslationKey())), mouseX, mouseY);
                }
            }
            i++;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

//        renderWeapon(graphics, left, top, 1);
        renderGun(graphics, left, top, mouseX, mouseY, getGun());
        graphics.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);

        var id = 0;
        for (var att : getGunAttachments(getGun()).keySet()) {
            var slotPos = getSlotPos(id);

            graphics.blit(SLOT, slotPos.x, slotPos.y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);

            if (!this.menu.getSlot(id).hasItem())
                graphics.blit(att.getIcon(), slotPos.x + 1, slotPos.y + 1, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            id++;
        }

        if(clickedSlot != -1){
            var slotPos = getSlotPos(clickedSlot);
            graphics.blit(GUI_TEXTURES, slotPos.x - 4, top + 82, 0, 214, 26, 28, 256, 256);
        }
    }

    public static ArrayList<ItemStack> findAttachments(Container inventory, AttachmentType type){
        var result = new ArrayList<ItemStack>();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var stack = inventory.getItem(i);
            if (stack.getItem() instanceof IAttachment attachment
                    && attachment.getType() == type && !contains(result, stack)) {
                result.add(stack);
            }
        }

        return result;
    }

    public static boolean contains(ArrayList<ItemStack> arrayList, ItemStack stack){
        for (var item: arrayList) {
            if(item.getItem() == stack.getItem()){
                return true;
            }
        }
        return false;
    }

    public Pos2I getSlotPos(int id) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        return new Pos2I(left + ATTACHMENT_X + id * SLOT_SIZE, top + ATTACHMENT_Y);
    }

    public int getSlotId(int mouseX, int mouseY) {
        var attCount = getGunAttachments(getGun()).keySet().size();

        for (var id = 0; id < attCount; id++) {
            var slotPos = getSlotPos(id);
            if(isMouseWithin(mouseX, mouseY, slotPos.x + 1, slotPos.y + 1, ICON_SIZE, ICON_SIZE))
                return id;
        }

        return -1;
    }

    public void renderGun(GuiGraphics graphics, int startX, int startY, int mouseX, int mouseY, ItemStack currentItem) {
//        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        ModelRenderUtil.scissor(startX + 8, startY + 17, 160, 70);

        var poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        {
            poseStack.translate(startX + 88, startY + 60, 100);
            poseStack.scale(50F, -50F, 50F);

//            graphics.pose().pushPose();
//            graphics.pose().translate(windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
//            graphics.pose().translate(0, windowY + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);
//            graphics.pose().mulPose(Axis.XP.rotationDegrees(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0)));
//            graphics.pose().mulPose(Axis.YP.rotationDegrees(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0)));

            RenderSystem.applyModelViewMatrix();
            var buffer = minecraft.renderBuffers().bufferSource();
            minecraft.getItemRenderer().render(currentItem, ItemDisplayContext.FIXED,
                    false, graphics.pose(), buffer, 15728880,
                    OverlayTexture.NO_OVERLAY, ModelRenderUtil.getModel(currentItem));
            buffer.endBatch();
        }
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
//        renderHelp(graphics);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

//        renderGun(graphics, left, top, mouseX, mouseY, getGun());

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        if (isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70)) {
            if (scroll < 0 && this.windowZoom > 0) {
                this.showHelp = false;
                this.windowZoom--;
            } else if (scroll > 0) {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        clickedSlot = getSlotId((int)mouseX, (int)mouseY);

        if (isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70)) {
            if (!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;
                this.showHelp = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.mouseGrabbed) {
            if (this.mouseGrabbedButton == 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.mouseGrabbed = false;
                this.windowX += (mouseX - this.mouseClickedX - 1);
                this.windowY += (mouseY - this.mouseClickedY);
            } else if (mouseGrabbedButton == 1 && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.mouseGrabbed = false;
                this.windowRotationX += (mouseX - this.mouseClickedX);
                this.windowRotationY -= (mouseY - this.mouseClickedY);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected ItemStack getGun() {
        return this.minecraft.player.getMainHandItem();
    }

    private void renderHelp(GuiGraphics graphics) {
        if (this.showHelp) {
            graphics.pose().pushPose();
            graphics.pose().scale(0.5F, 0.5F, 0.5F);
            graphics.drawString(minecraft.font, I18n.get(WINDOW_HELP), 56, 38, 0xFFFFFF, false);
            graphics.pose().popPose();
        }
    }

    private boolean isMouseWithinSlot(int mouseX, int mouseY, int left, int top, int i) {
        return isMouseWithin(mouseX, mouseY, left + ATTACHMENT_X + i * SLOT_SIZE, top + ATTACHMENT_Y, SLOT_SIZE, SLOT_SIZE);
    }

    private List<MiniButton> gatherButtons() {
        var buttons = new ArrayList<MiniButton>();
        if (!Config.CLIENT.hideConfigButton.get()) {
            var configButton = new MiniButton(0, 0, 192, 0, GUI_TEXTURES, onPress -> this.openConfigScreen());
            configButton.setTooltip(Tooltip.create(CONFIG_TOOLTIP));
            buttons.add(configButton);
        }
        return buttons;
    }

    private boolean isCompatible(ItemStack stack, AttachmentSlot slot) {
        if (stack.isEmpty()) return true;
        if (!(stack.getItem() instanceof IAttachment<?> attachment)) return false;
        if (!attachment.getType().equals(slot.getType())) return true;
        if (!attachment.canAttachTo(stack)) return false;

        return slot.mayPlace(stack);
    }

    private void openConfigScreen() {
        ModList.get().getModContainerById(Ntgl.MOD_ID).ifPresent(container -> {
            Screen screen = container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class).map(function -> function.screenFunction().apply(this.minecraft, null)).orElse(null);
            if (screen != null) {
                this.minecraft.setScreen(screen);
            } else if (this.minecraft != null && this.minecraft.player != null) {
                MutableComponent modName = literal("Configured");
                modName.setStyle(modName.getStyle().withColor(ChatFormatting.YELLOW).withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translatable("ntgl.chat.open_curseforge_page"))).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured")));
                Component message = translatable("ntgl.chat.install_configured", modName);
                this.minecraft.player.displayClientMessage(message, false);
            }
        });
    }
}