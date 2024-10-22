package com.nukateam.ntgl.common.base.config.gun;

import com.google.gson.internal.reflect.ReflectionHelper;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.AmmoContext;
import com.nukateam.ntgl.common.base.holders.*;
import com.nukateam.ntgl.common.base.utils.NbtUtils;
import com.nukateam.ntgl.common.data.annotation.Ignored;
import com.nukateam.ntgl.common.data.annotation.Optional;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.util.GunJsonUtil;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.widget.DebugButton;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Scope;
import com.nukateam.ntgl.common.helpers.BackpackHelper;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static com.nukateam.example.common.data.utils.ResourceUtils.resourceExists;
import static com.nukateam.ntgl.client.ClientHandler.*;
import static com.nukateam.ntgl.common.base.config.gun.Display.*;
import static com.nukateam.ntgl.common.base.config.gun.Modules.*;

public class Gun implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String ATTACHMENTS = "Attachments";
    protected General general = new General();
    protected Sounds sounds = new Sounds();
    protected Display display = new Display();
    protected Modules modules = new Modules();
    protected Map<String, ResourceLocation> textures = new HashMap<>();
    @Ignored
    protected Map<String, ResourceLocation> preparedTextures = new HashMap<>();

    public General getGeneral() {
        return this.general;
    }

    public Sounds getSounds() {
        return this.sounds;
    }

    public Display getDisplay() {
        return this.display;
    }

    public Modules getModules() {
        return this.modules;
    }

    public Map<String, ResourceLocation> getTextures() {
        return preparedTextures;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Gun");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ItemStack heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
            ItemStack scope = Gun.getScopeStack(heldItem);
            if (scope.getItem() instanceof ScopeItem scopeItem) {
                widgets.add(Pair.of(scope.getItem().getName(scope), () -> new DebugButton(Component.literal("Edit"), btn -> {
                    Minecraft.getInstance().setScreen(createEditorScreen(Debug.getScope(scopeItem)));
                })));
            }

            widgets.add(Pair.of(this.modules.getEditorLabel(), () -> new DebugButton(Component.literal(">"), btn -> {
                Minecraft.getInstance().setScreen(createEditorScreen(this.modules));
            })));
        });
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("General", this.general.serializeNBT());
        tag.put("Sounds", this.sounds.serializeNBT());
        tag.put("Display", this.display.serializeNBT());
        tag.put("Modules", this.modules.serializeNBT());
        tag.put("Textures", NbtUtils.serializeStringMap(this.textures));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("General", Tag.TAG_COMPOUND)) {
            this.general.deserializeNBT(tag.getCompound("General"));
        }
        if (tag.contains("Sounds", Tag.TAG_COMPOUND)) {
            this.sounds.deserializeNBT(tag.getCompound("Sounds"));
        }
        if (tag.contains("Display", Tag.TAG_COMPOUND)) {
            this.display.deserializeNBT(tag.getCompound("Display"));
        }
        if (tag.contains("Modules", Tag.TAG_COMPOUND)) {
            this.modules.deserializeNBT(tag.getCompound("Modules"));
        }
        if (tag.contains("Textures", Tag.TAG_COMPOUND)) {
            this.textures = NbtUtils.deserializeRLMap(tag.getCompound("Textures"));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.add("general", this.general.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "sounds", this.sounds.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "display", this.display.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modules", this.modules.toJsonObject());
        return object;
    }

    public static Gun create(ResourceLocation id, CompoundTag tag) {
        var gun = new Gun();
        gun.deserializeNBT(tag);
        prepareTextures(id.getPath(), gun);
        return gun;
    }

    public void onCreated(String id){
        prepareTextures(id, this);
    }

    private static void prepareTextures(String itemId, Gun gun) {
        var thread = new Thread(() ->
            gun.textures.forEach((variant, path) -> {
                var texture = resourceExists(path) ? path : getTexture(itemId, path);
                gun.preparedTextures.put(variant, texture);
            })
        );
        thread.start();
    }

    @NotNull
    private static ResourceLocation getTexture(String itemId, ResourceLocation path) {
        return new ResourceLocation(path.getNamespace(), "textures/guns/" + itemId + "/" + path.getPath() + ".png");
    }

    public Gun copy() {
        var gun = new Gun();
        gun.general = this.general.copy();
        gun.sounds = this.sounds.copy();
        gun.display = this.display.copy();
        gun.modules = this.modules.copy();
        return gun;
    }

    public boolean canAttachType(@Nullable AttachmentType type, Gun gun) {
        var attachments = gun.getModules().getAttachments();
        if(attachments == null)
            return false;
        return attachments.containsKey(type);
    }

//    @Nullable
//    public ScaledPositioned getAttachmentPosition(ResourceLocation type) {
//        if (this.modules.attachments != null && this.modules.attachments.containsKey(type)) {
//            if (type.equals(SCOPE)) {
//                return this.modules.attachments.get(type);
//            } else if (type.equals(BARREL)) {
//                return this.modules.attachments.barrelItem;
//            } else if (type.equals(STOCK)) {
//                return this.modules.attachments.stock;
//            } else if (type.equals(UNDER_BARREL)) {
//                return this.modules.attachments.underBarrel;
//            }
//        }
//        return null;
//    }

    public boolean canAimDownSight() {
        return /*this.canAttachType(SCOPE, ) || */this.modules.zoom != null;
    }

    public static ItemStack getScopeStack(ItemStack gun) {
        var compound = gun.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound(ATTACHMENTS);
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound("Scope"));
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasAttachmentEquipped(ItemStack stack, Gun gun, AttachmentType type) {
        if (!gun.canAttachType(type, gun))
            return false;

        var compound = stack.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound(ATTACHMENTS);
            return attachment.contains(type.toString(), Tag.TAG_COMPOUND);
        }
        return false;
    }

    @Nullable
    public static Scope getScope(ItemStack gun) {
        var compound = gun.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            var attachment = compound.getCompound(ATTACHMENTS);
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                var scopeStack = ItemStack.of(attachment.getCompound("Scope"));
                Scope scope = null;
                if (scopeStack.getItem() instanceof ScopeItem scopeItem) {
                    if (Ntgl.isDebugging()) {
                        return Debug.getScope(scopeItem);
                    }
                    scope = scopeItem.getProperties();
                }
                return scope;
            }
        }
        return null;
    }

    public ArrayList<Attachment> getAttachments(ArrayList<ItemStack> itemStacks) {
        var result = new ArrayList<Attachment>();

        for (var stack : itemStacks) {
            var item = stack.getItem();
            var itemRegistryName = ForgeRegistries.ITEMS.getKey(stack.getItem());
            var attachment = findAttachment(item, itemRegistryName);

            if(attachment != null)
                result.add(attachment);
        }
        return result;
    }

    private Attachment findAttachment(Item item, ResourceLocation itemRegistryName) {
        if(item instanceof IAttachment attachmentItem){
            var attachmentType = attachmentItem.getType();

            if(!getModules().getAttachments().containsKey(attachmentType))
                return null;

            var attachments = getModules().getAttachments().get(attachmentType);

            for (var attachment : attachments) {
                if(attachment.getItem() != null && attachment.getItem().equals(itemRegistryName)){
                    return attachment;
                }
            }
        }
        return null;
    }

    public static ArrayList<ItemStack> getAttachmentItems(ItemStack gun) {
        var compound = gun.getTag();
        var result = new ArrayList<ItemStack>();

        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            var attachment = compound.getCompound(ATTACHMENTS);
            for (var slot: attachment.getAllKeys()){
                if (attachment.contains(slot, Tag.TAG_COMPOUND))
                    result.add(ItemStack.of(attachment.getCompound(slot)));
            }
        }
        return result;
    }

    public static ItemStack getAttachmentItem(AttachmentType type, ItemStack gun) {
        var compound = gun.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            var attachment = compound.getCompound(ATTACHMENTS);
            if (attachment.contains(type.toString(), Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound(type.toString()));
            }
        }
        return ItemStack.EMPTY;
    }

    public static float getAdditionalDamage(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getFloat("AdditionalDamage");
    }

    public static AmmoContext findAmmo(LivingEntity entity, ItemStack weapon) {
        var id = GunModifierHelper.getCurrentAmmo(weapon);

        if (entity instanceof Player player) {
            var context = findPlayerAmmo(player, id);

            if(context == AmmoContext.NONE){
                var set = GunModifierHelper.getAmmoItems(weapon);
                for (var value: set) {
                    if(!value.equals(id) && Gun.getAmmo(weapon) == 0){
                        id = value;
                        context = findPlayerAmmo(player, id);
                        if(context != AmmoContext.NONE) {
                            GunModifierHelper.setCurrentAmmo(weapon, id);
                            return context;
                        }
                    }
                }
            }

            return context;
        }
        return getCreativeAmmoContext(id);
    }

    public static AmmoContext findMagazine(LivingEntity entity, ItemStack weapon) {
        var id = GunModifierHelper.getCurrentAmmo(weapon);

        if (entity instanceof Player player) {
            var context = findPlayerMagazine(player, id);

            if(context == AmmoContext.NONE){
                var set = GunModifierHelper.getAmmoItems(weapon);
                for (var value: set) {
                    if(!value.equals(id) && Gun.getAmmo(weapon) == 0){
                        id = value;
                        context = findPlayerMagazine(player, id);
                        if(context != AmmoContext.NONE) {
                            GunModifierHelper.setCurrentAmmo(weapon, id);
                            return context;
                        }
                    }
                }
            }

            return context;
        }

        return getCreativeAmmoContext(id);
    }

    public static AmmoContext findPlayerAmmo(Player player, ResourceLocation id) {
        if (player.isCreative())
            return getCreativeAmmoContext(id);

        var context = findAmmo(player.getInventory(), id);
        if (!context.equals(AmmoContext.NONE))
            return context;

        if (Ntgl.backpackedLoaded) {
            return BackpackHelper.findAmmo(player, id);
        }
        return AmmoContext.NONE;
    }

    public static AmmoContext findAmmo(Container inventory, ResourceLocation id){
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var stack = inventory.getItem(i);
            if (isAmmo(stack, id)) {
                return new AmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static AmmoContext findPlayerMagazine(Player player, ResourceLocation id) {
        if (player.isCreative()) {
            return getCreativeAmmoContext(id);
        }

        var context = findMagazine(player.getInventory(), id);
        if (!context.equals(AmmoContext.NONE))
            return context;

        if (Ntgl.backpackedLoaded) {
            return BackpackHelper.findMagazine(player, id);
        }

        return AmmoContext.NONE;
    }

    public static AmmoContext findMagazine(Container inventory, ResourceLocation id){
        ItemStack ammoStack = null;

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var foundStack = inventory.getItem(i);

            if (isAmmo(foundStack, id)) {
                if (foundStack.getDamageValue() == 0)
                    return new AmmoContext(foundStack, inventory);

                if (ammoStack == null || hasMoreAmmo(ammoStack, foundStack))
                    ammoStack = foundStack;
            }
        }

        if (ammoStack != null)
            return new AmmoContext(ammoStack, inventory);

        return AmmoContext.NONE;
    }

    private static ResourceLocation getKey(ItemStack stack){
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    /**
     * @return True if the second stack contains more ammo then the first
     */
    private static boolean hasMoreAmmo(ItemStack first, ItemStack second) {
        return second.getDamageValue() < first.getDamageValue() && first.getDamageValue() < first.getMaxDamage();
    }

    @NotNull
    private static AmmoContext getCreativeAmmoContext(ResourceLocation id) {
        var item = ForgeRegistries.ITEMS.getValue(id);
        var ammo = item != null ? new ItemStack(item, Integer.MAX_VALUE) : ItemStack.EMPTY;
        return new AmmoContext(ammo, null);
    }

    public static boolean isAmmo(ItemStack stack, ResourceLocation id) {
        return stack != null && Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()), id);
    }

    public static int getAmmo(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getInt(Tags.AMMO_COUNT);
    }

    public static void setAmmo(ItemStack gunStack, int amount) {
        var tag = gunStack.getOrCreateTag();
        tag.putInt(Tags.AMMO_COUNT, amount);
    }

    public static boolean hasAmmo(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getBoolean("IgnoreAmmo") || tag.getInt(Tags.AMMO_COUNT) > 0;
    }

    public static void fillAmmo(ItemStack gunStack) {
        if (gunStack.getItem() instanceof GunItem gunItem) {
            var tag = gunStack.getOrCreateTag();
//            var maxAmmo = gunItem.getModifiedGun(gunStack).getGeneral().getMaxAmmo(gunStack);
            var maxAmmo = GunModifierHelper.getMaxAmmo(gunStack);

            tag.putInt(Tags.AMMO_COUNT, maxAmmo);
        }
    }

    public static float getFovModifier(ItemStack stack, Gun modifiedGun) {
        float modifier = 0.0F;
        if (hasAttachmentEquipped(stack, modifiedGun, AttachmentType.SCOPE)) {
            var scope = Gun.getScope(stack);
            if (scope != null) {
                if (scope.getFovModifier() < 1.0F) {
                    return Mth.clamp(scope.getFovModifier(), 0.01F, 1.0F);
                }
                modifier -= scope.getFovModifier();
            }
        }
        var zoom = modifiedGun.getModules().getZoom();
        return zoom != null ? modifier + zoom.getFovModifier() : 0F;
    }

    public static class Builder {
        private final Gun gun;

        private Builder() {
            this.gun = new Gun();
        }

        private Builder(Gun gun) {
            this.gun = gun.copy();
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(Gun gun) {
            return new Builder(gun);
        }

        public Gun build() {
            return this.gun.copy(); //Copy since the builder could be used again
        }

        public Gun.Builder addAmmo(ResourceLocation id) {
            this.gun.general.ammo.add(id);
            return this;
        }

        public Builder setFireRate(int rate) {
            this.gun.general.rate = rate;
            return this;
        }

        public Builder setGripType(GripType gripType) {
            this.gun.general.gripType = gripType;
            return this;
        }

        public Builder setReloadType(ResourceLocation reloadType) {
            this.gun.general.reloadType = reloadType;
            return this;
        }

        public Builder setMaxAmmo(int maxAmmo) {
            this.gun.general.maxAmmo = maxAmmo;
            return this;
        }

        public Builder setReloadAmount(int reloadAmount) {
            this.gun.general.reloadAmount = reloadAmount;
            return this;
        }

        public Builder setReloadTime(int reloadTime) {
            this.gun.general.reloadTime = reloadTime;
            return this;
        }

        public Builder setLoadingType(LoadingType loadingType) {
            this.gun.general.loadingType = loadingType;
            return this;
        }

        public Builder setCategory(String category) {
            this.gun.general.category = category;
            return this;
        }

        public Builder setRecoilAngle(float recoilAngle) {
            this.gun.general.recoilAngle = recoilAngle;
            return this;
        }

        public Builder setRecoilKick(float recoilKick) {
            this.gun.general.recoilKick = recoilKick;
            return this;
        }

        public Builder setRecoilDurationOffset(float recoilDurationOffset) {
            this.gun.general.recoilDurationOffset = recoilDurationOffset;
            return this;
        }

        public Builder setRecoilAdsReduction(float recoilAdsReduction) {
            this.gun.general.recoilAdsReduction = recoilAdsReduction;
            return this;
        }

        public Builder setProjectileAmount(int projectileAmount) {
            this.gun.general.projectileAmount = projectileAmount;
            return this;
        }

        public Builder setAlwaysSpread(boolean alwaysSpread) {
            this.gun.general.alwaysSpread = alwaysSpread;
            return this;
        }

        public Builder setSpread(float spread) {
            this.gun.general.spread = spread;
            return this;
        }

        public Builder setFireSound(SoundEvent sound) {
            this.gun.sounds.fire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setReloadSound(SoundEvent sound) {
            this.gun.sounds.reload = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setCockSound(SoundEvent sound) {
            this.gun.sounds.cock = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setSilencedFireSound(SoundEvent sound) {
            this.gun.sounds.silencedFire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setEnchantedFireSound(SoundEvent sound) {
            this.gun.sounds.enchantedFire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setMuzzleFlash(double size, double xOffset, double yOffset, double zOffset) {
            var flash = new Flash();
            flash.size = size;
            flash.xOffset = xOffset;
            flash.yOffset = yOffset;
            flash.zOffset = zOffset;
            this.gun.display.flash = flash;
            return this;
        }

        public Builder setZoom(float fovModifier, double xOffset, double yOffset, double zOffset) {
            var zoom = new Zoom();
            zoom.fovModifier = fovModifier;
            zoom.xOffset = xOffset;
            zoom.yOffset = yOffset;
            zoom.zOffset = zOffset;
            this.gun.modules.zoom = zoom;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setZoom(Zoom.Builder builder) {
            this.gun.modules.zoom = builder.build();
            return this;
        }
    }
}
