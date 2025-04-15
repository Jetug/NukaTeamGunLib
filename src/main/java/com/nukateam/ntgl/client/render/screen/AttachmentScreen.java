package com.nukateam.ntgl.client.render.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.nukateam.ntgl.*;
import com.nukateam.ntgl.client.event.InputEvents;
import com.nukateam.ntgl.client.render.screen.widget.*;
import com.nukateam.ntgl.client.util.util.render.ModelRenderUtil;
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
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraftforge.client.*;
import net.minecraftforge.fml.ModList;
import org.lwjgl.glfw.GLFW;
import java.util.*;

import static com.nukateam.ntgl.client.util.util.render.ModelRenderUtil.*;
import static net.minecraft.network.chat.Component.*;

/**
 * Author: MrCrayfish
 */
public class AttachmentScreen extends AbstractContainerScreen<AttachmentContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("ntgl:textures/gui/attachments.png");
    private static final ResourceLocation SLOT = new ResourceLocation("ntgl:textures/gui/slot.png");
    private static final Component CONFIG_TOOLTIP = translatable("ntgl.button.config.tooltip");
    public static final String ATTACHMENT_NOT_APPLICABLE = "slot.ntgl.attachment.not_applicable";
    public static final String ATTACHMENT_INCOMPATIBLE = "slot.ntgl.attachment.incompatible";
    public static final String WINDOW_HELP = "container.ntgl.attachments.window_help";
    public static final int SLOT_SIZE = 18;
    public static final int IMAGE_HEIGHT = 214;
    public static final int ATTACHMENT_Y = 107;
    public static final int ATTACHMENT_X = 7;
    public static final int ICON_SIZE = 16;

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

        var size = weaponInventory.getContainerSize();

        for(var i = 0; i < size; i++){
            var slot = this.menu.getSlot(i);
            if (isMouseWithinSlot(mouseX, mouseY, left, top, i)
                    && slot instanceof AttachmentSlot attachmentSlot) {
                renderAttachmentTooltip(graphics, mouseX, mouseY, attachmentSlot);
            }
            i++;
        }
//        var attachments = getGunAttachments(getGun());
//        var i = 0;
//        for (var entry : attachments.entrySet()) {
//            var attachmentType = entry.getKey();
//            if (isMouseWithinSlot(mouseX, mouseY, left, top, i)) {
//                var slot = this.menu.getSlot(i);
//                renderAttachmentTooltip(graphics, mouseX, mouseY, slot, i);
//            }
//            i++;
//        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

//        renderWeapon(graphics, left, top, 1);
        renderGun(graphics, left, top, mouseX, mouseY, getGun());
        graphics.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);

//        var attachments = getGunAttachments(getGun());

        for(int i = 0; i < weaponInventory.getContainerSize(); i++) {
            var slotPos = getAttachmentBgPos(i);
            var slot = this.menu.getSlot(i);
            if(slot instanceof AttachmentSlot attachmentSlot) {
                renderAttachmentSlot(graphics, attachmentSlot, slotPos);
            }
        }
//        var id = 0;
//        for (var att : attachments.keySet()) {
//            var slotPos = getAttachmentBgPos(id);
//
//            graphics.blit(SLOT, slotPos.x, slotPos.y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
//            var slot = this.menu.getSlot(id);
//            if(slot instanceof AttachmentSlot attachmentSlot) {
//                if (!attachmentSlot.hasItem()) {
//                    graphics.blit(att.getIcon(),
//                            slotPos.x + 1, slotPos.y + 1,
//                            0, 0,
//                            ICON_SIZE, ICON_SIZE,
//                            ICON_SIZE, ICON_SIZE);
//                }
//            }
//            id++;
//        }

//        if(clickedSlot != -1){
//            var slot = (AttachmentSlot)menu.getSlot(clickedSlot);
//            var attachments = findAttachments(playerInventory, slot.getType());
//
//            for (int i = 0; i < attachments.size(); i++) {
//                var pos = getAttachmentBgPos(clickedSlot, i);
//                var slotPos = getAttachmentSlotPos(clickedSlot, i);
//
//                graphics.blit(GUI_TEXTURES, pos.x, pos.y, 0, 214, 26, 28, 256, 256);
//                graphics.renderItem(attachments.get(i), slotPos.x, slotPos.y);
//            }
//        }
    }

    private static void renderAttachmentSlot(GuiGraphics graphics, AttachmentSlot attachmentSlot, Pos2I slotPos) {
        graphics.blit(SLOT, slotPos.x, slotPos.y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        if (!attachmentSlot.hasItem()) {
            graphics.blit(
                    attachmentSlot.getType().getIcon(),
                    slotPos.x + 1, slotPos.y + 1,
                    0, 0,
                    ICON_SIZE, ICON_SIZE,
                    ICON_SIZE, ICON_SIZE
            );
        }
    }

    private final ArrayList<SlotButton> attachmentButtons = new ArrayList<>();

    public Pos2I getAttachmentBgPos(int clickedSlot, int id) {
        var slotPos = getAttachmentBgPos(clickedSlot);
        int top = (this.height - this.imageHeight) / 2;
        return new Pos2I(slotPos.x - 4, top + 80 - 23 * id);
    }

    public Pos2I getAttachmentSlotPos(int clickedSlot, int id) {
        var pos = getAttachmentBgPos(clickedSlot, id);
        var startX = pos.x + 5;
        var startY = pos.y + 6;
        return new Pos2I(startX, startY);
    }

    public static ArrayList<ItemStack> findAttachments(Container inventory, AttachmentType type){
        var result = new ArrayList<ItemStack>();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var stack = inventory.getItem(i);
            if (stack.getItem() instanceof IAttachment attachment
                    && attachment.getType() == type) {
                result.add(stack);
            }
        }

        return result;
    }

    public Pos2I getAttachmentBgPos(int id) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        return new Pos2I(left + ATTACHMENT_X + id * SLOT_SIZE, top + ATTACHMENT_Y);
    }

//    public int getSlotId(int mouseX, int mouseY) {
//        var attCount = getAttachmentTypes(getGun()).keySet().size();
//
//        for (var id = 0; id < attCount; id++) {
//            var slotPos = getAttachmentBgPos(id);
//            if(isMouseWithin(mouseX, mouseY, slotPos.x + 1, slotPos.y + 1, ICON_SIZE, ICON_SIZE))
//                return id;
//        }
//
//        return -1;
//    }

    protected void renderAttachmentTooltip(GuiGraphics graphics, int mouseX, int mouseY, AttachmentSlot attachmentSlot) {
        if (!attachmentSlot.isActive()) {
            graphics.renderComponentTooltip(this.font,
                    List.of((translatable(attachmentSlot.getType().getTranslationKey())),
                            translatable(ATTACHMENT_NOT_APPLICABLE)), mouseX, mouseY);
        }
        else if (attachmentSlot.getItem().isEmpty() && !this.isCompatible(this.menu.getCarried(), attachmentSlot)) {
            graphics.renderComponentTooltip(this.font,
                    List.of(translatable(ATTACHMENT_INCOMPATIBLE)
                            .withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
        }
        else if (attachmentSlot.getItem().isEmpty()) {
            graphics.renderComponentTooltip(this.font,
                    List.of(translatable(attachmentSlot.getType().getTranslationKey())),
                    mouseX, mouseY);
        }
    }

    protected void renderGun(GuiGraphics graphics, int startX, int startY, int mouseX, int mouseY, ItemStack currentItem) {
        var poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        {
            poseStack.translate(startX + 88, startY + 60, 100);
            poseStack.scale(50F, -50F, 50F);
            poseStack.pushPose();
            {
                poseStack.mulPose(Axis.XP.rotation(0 + InputEvents.X / 10f));
                poseStack.mulPose(Axis.YP.rotation(3.7f + InputEvents.Y / 10f));
                poseStack.mulPose(Axis.ZP.rotation(-0.3f + InputEvents.Z / 10f));
                RenderSystem.applyModelViewMatrix();

                var buffer = minecraft.renderBuffers().bufferSource();

                minecraft.getItemRenderer().render(currentItem, ItemDisplayContext.FIXED,
                        false, graphics.pose(), buffer, 15728880,
                        OverlayTexture.NO_OVERLAY, ModelRenderUtil.getModel(currentItem));
                buffer.endBatch();
            }
            poseStack.popPose();
        }
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

//    public void renderGun(GuiGraphics graphics, int startX, int startY, int mouseX, int mouseY, ItemStack currentItem) {
//        var poseStack = RenderSystem.getModelViewStack();
//        poseStack.pushPose();
//        {
////            poseStack.mulPose(Axis.XP.rotation(InputEvents.X));
////            poseStack.mulPose(Axis.YP.rotation(InputEvents.Y));
////            poseStack.mulPose(Axis.ZP.rotation(InputEvents.Z));
//
////            poseStack.translate(startX + 88, startY + 60, 100);
//            poseStack.translate(startX, startY, 0);
////            poseStack.scale(50F, -50F, 50F);
//
//            poseStack.translate(InputEvents.X, InputEvents.Y, InputEvents.Z);
//            RenderSystem.applyModelViewMatrix();
//
//            var buffer = minecraft.renderBuffers().bufferSource();
////            minecraft.getItemRenderer().render(currentItem, ItemDisplayContext.FIXED,
////                    false, graphics.pose(), buffer, 15728880,
////                    OverlayTexture.NO_OVERLAY, ModelRenderUtil.getModel(currentItem));
//
//            var gun = (GunItem)currentItem.getItem();
//
//            Minecraft.getInstance().getItemRenderer().renderStatic(currentItem, ItemDisplayContext.NONE, OverlayTexture.NO_OVERLAY,
//                    15728880, poseStack, buffer, minecraft.level, 0);
//
//            gun.getRenderer().render(minecraft.player,currentItem,ItemDisplayContext.FIXED,
//                    poseStack, buffer, RenderType.solid(), buffer.getBuffer(RenderType.solid()), OverlayTexture.NO_OVERLAY);
//
//            buffer.endBatch();
//        }
//        poseStack.popPose();
//        RenderSystem.applyModelViewMatrix();
//    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
//        renderHelp(graphics);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

//        renderGun(graphics, left, top, mouseX, mouseY, getGun());

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

//        clickedSlot = getSlotId((int)mouseX, (int)mouseY);
//
//        if(clickedSlot != -1 && attachmentButtons.isEmpty()) {
//            var slot = (AttachmentSlot)menu.getSlot(clickedSlot);
//            var attachments = findAttachments(playerInventory, slot.getType());
//
//            for (int i = 0; i < attachments.size(); i++) {
//                var slotPos = getAttachmentSlotPos(clickedSlot, i);
//
//                this.addWidget(new SlotButton(slotPos.x, slotPos.y, attachments.get(i), (b) -> {
//                    var stack = ((SlotButton) b).getStack();
//                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachmentChanged(menu.containerId, stack, getGun()));
//                }));
//            }
//        }
//        else {
//            attachmentButtons.clear();
//        }

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
//
//    private List<MiniButton> gatherButtons2() {
//        var buttons = new ArrayList<MiniButton>();
//        var configButton = new SlotButton(0, 0, onPress -> this.openConfigScreen());
//        configButton.setTooltip(Tooltip.create(CONFIG_TOOLTIP));
//        buttons.add(configButton);
//        return buttons;
//    }

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