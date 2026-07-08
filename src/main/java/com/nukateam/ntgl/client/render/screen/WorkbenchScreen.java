package com.nukateam.ntgl.client.render.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.nukateam.example.common.registery.ExampleWeapons;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.helpers.render.ModelRenderUtil;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.blockentity.WorkbenchBlockEntity;
import com.nukateam.ntgl.common.foundation.container.WorkbenchContainer;
import com.nukateam.ntgl.modules.crafting.recipe.WorkbenchIngredient;
import com.nukateam.ntgl.modules.crafting.recipe.WorkbenchRecipe;
import com.nukateam.ntgl.modules.crafting.recipe.WorkbenchRecipes;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageCraft;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import com.nukateam.ntgl.modules.crafting.registry.ModRecipeTypes;
import com.nukateam.ntgl.modules.datapack.managers.NetworkWeaponManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Author: MrCrayfish
 */
public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchContainer> {
    private static final ResourceLocation GUI_BASE = ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "textures/gui/workbench.png");
    private static boolean showRemaining = false;

    private Tab currentTab;
    private List<Tab> tabs = new ArrayList<>();
    private List<MaterialItem> materials;
    private List<MaterialItem> filteredMaterials;
    private Inventory playerInventory;
    private WorkbenchBlockEntity workbench;
    private Button btnCraft;
    private CheckBox checkBoxMaterials;
    private ItemStack displayStack = ItemStack.EMPTY;

    public WorkbenchScreen(WorkbenchContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.workbench = container.getWorkbench();
        this.imageWidth = 275;
        this.imageHeight = 184;
        this.materials = new ArrayList<>();
        var s = playerInventory.player.level().getRecipeManager().getRecipes().size();
        var d = playerInventory.player.level().getRecipeManager().getAllRecipesFor(ModRecipeTypes.WORKBENCH.get()).size();

        s = d;
        d = s;

        this.createTabs(WorkbenchRecipes.getAllHolders(playerInventory.player.level()));
        if (!this.tabs.isEmpty()) {
            this.imageHeight += 28;
        }
    }

    @Override
    public void init() {
        super.init();
        if (!this.tabs.isEmpty()) {
            this.topPos += 28;
        }

        this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            int index = this.currentTab.getCurrentIndex();
            if (index - 1 < 0) {
                this.loadItem(this.currentTab.getRecipes().size() - 1);
            } else {
                this.loadItem(index - 1);
            }
        }).pos(this.leftPos + 9, this.topPos + 18).size(15, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            int index = this.currentTab.getCurrentIndex();
            if (index + 1 >= this.currentTab.getRecipes().size()) {
                this.loadItem(0);
            } else {
                this.loadItem(index + 1);
            }
        }).pos(this.leftPos + 153, this.topPos + 18).size(15, 20).build());
        this.btnCraft = this.addRenderableWidget(Button.builder(Component.translatable("gui.ntgl.workbench.assemble"), button -> {
            int index = this.currentTab.getCurrentIndex();
            var holder = this.currentTab.getRecipes().get(index);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageCraft(holder.id(), this.workbench.getBlockPos()));
        }).pos(this.leftPos + 195, this.topPos + 16).size(74, 20).build());
        this.btnCraft.active = false;
        this.checkBoxMaterials = this.addRenderableWidget(new CheckBox(this.leftPos + 172, this.topPos + 51, Component.translatable("gui.ntgl.workbench.show_remaining")));
        this.checkBoxMaterials.setToggled(WorkbenchScreen.showRemaining);
        this.loadItem(this.currentTab.getCurrentIndex());
    }

    @Override
    public void containerTick() {
        super.containerTick();

        for (MaterialItem material : this.materials) {
            material.tick();
        }

        boolean canCraft = true;
        for (MaterialItem material : this.materials) {
            if (!material.isEnabled()) {
                canCraft = false;
                break;
            }
        }

        this.btnCraft.active = canCraft;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        WorkbenchScreen.showRemaining = this.checkBoxMaterials.isToggled();

        for (int i = 0; i < this.tabs.size(); i++) {
            if (ModelRenderUtil.isMouseWithin((int) mouseX, (int) mouseY, this.leftPos + 28 * i, this.topPos - 28, 28, 28)) {
                this.currentTab = this.tabs.get(i);
                this.loadItem(this.currentTab.getCurrentIndex());
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }

        return result;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int offset = this.tabs.isEmpty() ? 0 : 28;
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY - 28 + offset, 4210752, false);
        graphics.drawString(this.font, this.playerInventory.getDisplayName(), this.inventoryLabelX, this.inventoryLabelY - 9 + offset, 4210752, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);

        int startX = this.leftPos;
        int startY = this.topPos;

        for (int i = 0; i < this.tabs.size(); i++) {
            if (ModelRenderUtil.isMouseWithin(mouseX, mouseY, startX + 28 * i, startY - 28, 28, 28)) {
                this.setTooltipForNextRenderPass(Component.translatable(this.tabs.get(i).getTabKey()));
                this.renderTooltip(graphics, mouseX, mouseY);
                return;
            }
        }

        if (filteredMaterials == null) return;

        for (int i = 0; i < this.filteredMaterials.size(); i++) {
            int itemX = startX + 172;
            int itemY = startY + i * 19 + 63;
            if (ModelRenderUtil.isMouseWithin(mouseX, mouseY, itemX, itemY, 80, 19)) {
                MaterialItem materialItem = this.filteredMaterials.get(i);
                if (materialItem != MaterialItem.EMPTY) {
                    graphics.renderTooltip(this.font, materialItem.getDisplayStack(), mouseX, mouseY);
                    return;
                }
            }
        }

        if (ModelRenderUtil.isMouseWithin(mouseX, mouseY, startX + 8, startY + 38, 160, 48)) {
            graphics.renderTooltip(this.font, this.displayStack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        try {
            /* Fixes partial ticks to use percentage from 0 to 1 */
            partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

            int startX = this.leftPos;
            int startY = this.topPos;

            RenderSystem.enableBlend();

            /* Draw unselected tabs */
            drawUnselectedTabs(graphics, startX, startY);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(GUI_BASE, startX, startY, 0, 0, 173, 184);
            graphics.blit(GUI_BASE, startX + 173, startY, 78, 184, 173, 0, 1, 184, 256, 256);
            graphics.blit(GUI_BASE, startX + 251, startY, 174, 0, 24, 184);
            graphics.blit(GUI_BASE, startX + 172, startY + 16, 198, 0, 20, 20);

            /* Draw selected tab */
            drawSelectedTab(graphics, startX, startY);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (this.workbench.getItem(0).isEmpty()) {
                graphics.blit(GUI_BASE, startX + 174, startY + 18, 165, 199, 16, 16);
            }

            var currentItem = this.displayStack;
            var builder = new StringBuilder(currentItem.getHoverName().getString());

            if (currentItem.getCount() > 1) {
                builder.append(ChatFormatting.GOLD);
                builder.append(ChatFormatting.BOLD);
                builder.append(" x ");
                builder.append(currentItem.getCount());
            }

            graphics.drawCenteredString(this.font, builder.toString(), startX + 88, startY + 22, Color.WHITE.getRGB());

            renderGun(graphics, partialTicks, startX, startY, currentItem);

            this.filteredMaterials = this.getMaterials();

            for (int i = 0; i < this.filteredMaterials.size(); i++) {
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

                var materialItem = this.filteredMaterials.get(i);
                var stack = materialItem.getDisplayStack();

                if (!stack.isEmpty()) {
                    Lighting.setupForFlatItems();
                    if (materialItem.isEnabled()) {
                        graphics.blit(GUI_BASE, startX + 172, startY + i * 19 + 63, 0, 184, 80, 19);
                    } else {
                        graphics.blit(GUI_BASE, startX + 172, startY + i * 19 + 63, 0, 222, 80, 19);
                    }

                    var name = stack.getHoverName().getString();
                    if (this.font.width(name) > 55) {
                        name = this.font.plainSubstrByWidth(name, 50).trim() + "...";
                    }

                    graphics.drawString(this.font, name, startX + 172 + 22, startY + i * 19 + 6 + 63, Color.WHITE.getRGB());
                    graphics.renderItem(stack, startX + 172 + 2, startY + i * 19 + 1 + 63);

                    if (this.checkBoxMaterials.isToggled()) {
                        int count = InventoryUtil.getItemStackAmount(Minecraft.getInstance().player, stack);
                        stack = stack.copy();
                        stack.setCount(stack.getCount() - count);
                    }

                    graphics.renderItemDecorations(this.font, stack, startX + 172 + 2, startY + i * 19 + 1 + 63);
                }
            }
        } catch (Exception e) {
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    public static void renderGun(GuiGraphics graphics, float partialTicks, int startX, int startY, ItemStack currentItem) {
        var minecraft = Minecraft.getInstance();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ModelRenderUtil.scissor(startX + 8, startY + 17, 160, 70);

        var poseStack = graphics.pose();
        poseStack.pushPose();
        {
            poseStack.translate(startX + 88, startY + 60, 100);
            poseStack.scale(50F, -50F, 50F);
            poseStack.mulPose(Axis.XP.rotationDegrees(5F));
            poseStack.mulPose(Axis.YP.rotationDegrees(Minecraft.getInstance().player.tickCount + partialTicks));
            RenderSystem.applyModelViewMatrix();
            MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
            Minecraft.getInstance().getItemRenderer().render(currentItem, ItemDisplayContext.FIXED, false, graphics.pose(), buffer, 15728880, OverlayTexture.NO_OVERLAY, ModelRenderUtil.getModel(currentItem));
            buffer.endBatch();
        }
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public List<Tab> getTabs() {
        return ImmutableList.copyOf(this.tabs);
    }

    private void drawSelectedTab(GuiGraphics graphics, int startX, int startY) {
        if (this.currentTab != null) {
            int i = this.tabs.indexOf(this.currentTab);
            int u = i == 0 ? 80 : 108;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(GUI_BASE, startX + 28 * i, startY - 28, u, 214, 28, 32);
            graphics.renderItem(this.currentTab.getIcon(), startX + 28 * i + 6, startY - 28 + 8);
        }
    }

    private void drawUnselectedTabs(GuiGraphics graphics, int startX, int startY) {
        for (int i = 0; i < this.tabs.size(); i++) {
            var tab = this.tabs.get(i);
            if (tab != this.currentTab) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.blit(GUI_BASE, startX + 28 * i, startY - 28, 80, 184, 28, 32);
                graphics.renderItem(tab.getIcon(), startX + 28 * i + 6, startY - 28 + 8);
            }
        }
    }

    private List<MaterialItem> getMaterials() {
        List<MaterialItem> materials = NonNullList.withSize(6, MaterialItem.EMPTY);
        List<MaterialItem> filteredMaterials = this.materials.stream().filter(materialItem -> this.checkBoxMaterials.isToggled() ? !materialItem.isEnabled() : materialItem != MaterialItem.EMPTY).collect(Collectors.toList());
        for (int i = 0; i < filteredMaterials.size() && i < materials.size(); i++) {
            materials.set(i, filteredMaterials.get(i));
        }
        return materials;
    }

    private void loadItem(int index) {

        RecipeHolder<WorkbenchRecipe> holder = this.currentTab.getRecipes().get(index);

        WorkbenchRecipe recipe = holder.value();

        this.displayStack = recipe.result().copy();

        this.materials.clear();

        List<WorkbenchIngredient> ingredients = recipe.materials();

        if (ingredients != null) {

            for (WorkbenchIngredient ingredient : ingredients) {
                MaterialItem item = new MaterialItem(ingredient);
                item.updateEnabledState();
                this.materials.add(item);
            }

            this.currentTab.setCurrentIndex(index);
        }
    }


    public static class MaterialItem {
        public static final MaterialItem EMPTY = new MaterialItem();

        private long lastTime = System.currentTimeMillis();
        private int displayIndex;
        private boolean enabled = false;
        private WorkbenchIngredient ingredient;
        private final List<ItemStack> displayStacks = new ArrayList<>();

        private MaterialItem() {
        }

        private MaterialItem(WorkbenchIngredient ingredient) {
            this.ingredient = ingredient;
            Stream.of(ingredient.ingredient().getItems()).forEach(stack -> {
                ItemStack displayStack = stack.copy();
                displayStack.setCount(ingredient.count());
                this.displayStacks.add(displayStack);
            });
        }

        public WorkbenchIngredient getIngredient() {
            return this.ingredient;
        }

        public void tick() {
            if (this.ingredient == null) return;

            this.updateEnabledState();
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastTime >= 1000) {
                this.displayIndex = (this.displayIndex + 1) % this.displayStacks.size();
                this.lastTime = currentTime;
            }
        }

        public ItemStack getDisplayStack() {
            return this.ingredient != null ? this.displayStacks.get(this.displayIndex) : ItemStack.EMPTY;
        }

        public void updateEnabledState() {
            this.enabled = InventoryUtil.hasWorkstationIngredient(Minecraft.getInstance().player, this.ingredient);
        }

        public boolean isEnabled() {
            return this.ingredient == null || this.enabled;
        }
    }

    private void createTabs(List<RecipeHolder<WorkbenchRecipe>> recipes) {
        List<RecipeHolder<WorkbenchRecipe>> weapons = new ArrayList<>();
        List<RecipeHolder<WorkbenchRecipe>> attachments = new ArrayList<>();
        List<RecipeHolder<WorkbenchRecipe>> ammo = new ArrayList<>();
        List<RecipeHolder<WorkbenchRecipe>> misc = new ArrayList<>();

        for (var holder : recipes) {
            var recipe = holder.value();
            var output = recipe.result();
            if (output == null) continue;

            if (output.getItem() instanceof IWeapon) {
                weapons.add(holder);
            } else if (output.getItem() instanceof IAttachment) {
                attachments.add(holder);
            } else if (this.isAmmo(output)) {
                ammo.add(holder);
            } else {
                misc.add(holder);
            }
        }

        if (!weapons.isEmpty()) {
//            ItemStack icon = new ItemStack(ExampleWeapons.PISTOL.get());
//            icon.getOrCreateTag().putInt("AmmoCount", ExampleWeapons.PISTOL.get().getGun().getGeneral().getMaxAmmo());
//            this.tabs.add(new Tab(icon, "weapons", weapons));
//            var cat = new ArrayList<String>();
            var categoryRecipes = new HashMap<String, List<RecipeHolder<WorkbenchRecipe>>>();

            for (var recipe : weapons) {
                var weaponStack = recipe.value().result();
                var gunItem = (IWeapon) weaponStack.getItem();
                var category = gunItem.getModifiedConfig(weaponStack).getGeneral().getCategory();
                var buff = categoryRecipes.getOrDefault(category, new ArrayList<>());

                buff.add(recipe);
                categoryRecipes.put(category, buff);
            }

            for (var entry : categoryRecipes.entrySet()) {
                var recipeList = entry.getValue();
                var category = entry.getKey();

                if (!recipeList.isEmpty()) {
                    var item = recipeList.get(0).value().result().getItem();
                    var icon = new ItemStack(item);
                    var player = Minecraft.getInstance().player;
                    var gunData = new WeaponData(icon, player);
                    WeaponStateHelper.setMaxAmmo(gunData);

                    this.tabs.add(new Tab(icon, category, recipeList));
                }
            }
        }

        if (!attachments.isEmpty()) {
            this.tabs.add(new Tab(new ItemStack(ExampleWeapons.GRENADE.get()), "attachments", attachments));
        }

        if (!ammo.isEmpty()) {
            var item = ammo.get(0).value().result().getItem();
            var icon = new ItemStack(item);
//            this.tabs.add(new Tab(new ItemStack(ExampleWeapons.ROUND10MM.get()), "projectile", projectile));
            this.tabs.add(new Tab(icon, "projectile", ammo));
        }

        if (!misc.isEmpty()) {
            this.tabs.add(new Tab(new ItemStack(Items.BARRIER), "misc", misc));
        }

        if (!this.tabs.isEmpty()) {
            this.currentTab = this.tabs.get(0);
        }
    }

    private boolean isAmmo(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof IAmmo) return true;

        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Objects.requireNonNull(id);

        for (var gunItem : NetworkWeaponManager.getClientRegisteredWeapons()) {
            var ammo = gunItem.getConfig().getGeneral().getAmmo();

            for (var a : ammo) {
                if (a.getId().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class Tab {
        private final ItemStack icon;
        private final String id;
        private final List<RecipeHolder<WorkbenchRecipe>> items;
        private int currentIndex;

        public Tab(ItemStack icon, String id, List<RecipeHolder<WorkbenchRecipe>> items) {
            this.icon = icon;
            this.id = id;
            this.items = items;
        }

        public ItemStack getIcon() {
            return icon;
        }

        public String getTabKey() {
            return "gui.ntgl.workbench.tab." + id;
        }

        public void setCurrentIndex(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public List<RecipeHolder<WorkbenchRecipe>> getRecipes() {
            return items;
        }
    }
}
