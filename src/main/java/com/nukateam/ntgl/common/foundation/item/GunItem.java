package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.common.base.handlers.GunHandler;
import com.nukateam.ntgl.common.base.utils.FuelUtils;
import com.nukateam.ntgl.common.base.NetworkManager;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.geo.interfaces.DynamicGeoItem;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.renderers.gun.*;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.foundation.item.interfaces.*;
import com.nukateam.ntgl.modules.enchantment.EnchantmentTypes;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.core.animatable.instance.*;
import mod.azure.azurelib.core.animation.*;
import net.minecraft.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.*;
import javax.annotation.*;
import java.util.*;
import java.util.function.*;

import static com.nukateam.ntgl.common.data.constants.Tags.AMMO_COUNT;
import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class GunItem extends Item implements DynamicGeoItem, IColored, IMeta, IResourceProvider, IConfigConsumer<Gun>, IConfigProvider<Gun> {
    public static final String VARIANT = "variant";
    private final Lazy<String> name = Lazy.of(() -> ResourceUtils.getResourceName(getRegistryName()));
    private final WeakHashMap<CompoundTag, Gun> modifiedGunCache = new WeakHashMap<>();
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private final Lazy<DefaultGunRendererGeo> GUN_RENDERER = Lazy.of(() -> new DefaultGunRendererGeo());
    private Gun gun = new Gun();
    private GunHandler gunHandler = new GunHandler();

    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    protected IGunModifier[] modifiers;

    public GunItem(Item.Properties properties, IGunModifier... modifiers) {
        super(properties);
        this.modifiers = modifiers;
    }

    @Nullable
    public IGunModifier[] getGunModifiers() {
        return modifiers;
    }

    @OnlyIn(Dist.CLIENT)
    public DynamicGeoItemRenderer getRenderer() {
        return GUN_RENDERER.get();
    }

    @Override
    public BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<GunAnimator>, GunAnimator> getAnimatorFactory() {
        return GunAnimator::new;
    }

    @Override
    public void setConfig(NetworkManager.Supplier<Gun> supplier) {
        this.gun = supplier.getConfig();
        gun.onCreated(getName());
    }

    @Override
    public Gun getConfig() {
        return getGun();
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public String getNamespace() {
        return getRegistryName().getNamespace();
    }

    public Gun getGun() {
        return this.gun;
    }

    public GunHandler getGunHandler() {
        return gunHandler;
    }

    public GunItem setGunHandler(GunHandler gunHandler) {
        this.gunHandler = gunHandler;
        return this;
    }

    public static String getVariant(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains(VARIANT, Tag.TAG_STRING)) {
            tag.putString(VARIANT, "default");
        }

        return tag.getString(VARIANT);
    }

    public void setDefaultTag(CompoundTag tag){
        tag.putInt(AMMO_COUNT, getGun().getGeneral().getMaxAmmo());
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        var data = new GunData(stack, null);
        var ammo = ForgeRegistries.ITEMS.getValue(GunStateHelper.getAmmoId(data));

        if (ammo != null) {
            tooltip.add(Component.translatable("info.ntgl.ammo_type",
                            Component.translatable(ammo.getDescriptionId()).withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.GRAY));
        }

        var tagCompound = stack.getOrCreateTag();
        addAditionalDamage(tooltip, tagCompound, data);
        addAmmo(tooltip, tagCompound, data);
        addFuel(tooltip, tagCompound, data);
        //tooltip.add(Component.translatable("info.ntgl.attachment_help", new KeybindComponent("key.ntgl.attachments")
        // .getString().toUpperCase(Locale.ENGLISH))
        // .withStyle(ChatFormatting.YELLOW));
    }

    private static void addFuel(List<Component> tooltip, CompoundTag tagCompound, GunData gunData) {
        var allFuel = GunModifierHelper.getFuelTypes(gunData);
        for (var fuelType : allFuel) {
            int fuelAmount = FuelUtils.getFuel(gunData.gun, fuelType);
            tooltip.add(Component.translatable(fuelType.getDescriptionId(),
                    ChatFormatting.WHITE.toString()
                            + fuelAmount + "/"
                            + GunModifierHelper.getMaxFuel(gunData, fuelType)).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addAmmo(List<Component> tooltip, CompoundTag tagCompound, GunData gunData) {
        if (tagCompound.getBoolean("IgnoreAmmo")) {
            tooltip.add(Component.translatable("info.ntgl.ignore_ammo").withStyle(ChatFormatting.AQUA));
        } else {
            int ammoCount = tagCompound.getInt(AMMO_COUNT);
            tooltip.add(Component.translatable("info.ntgl.projectile",
                    ChatFormatting.WHITE.toString()
                            + ammoCount + "/"
                            + GunEnchantmentHelper.getAmmoCapacity(gunData)).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addAditionalDamage(List<Component> tooltip, CompoundTag tagCompound, GunData gunData) {
        var additionalDamageText = "";

        if (tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC)) {
            var additionalDamage = tagCompound.getFloat("AdditionalDamage");
            additionalDamage += GunModifierHelper.getAdditionalDamage(gunData);

            if (additionalDamage > 0) {
                additionalDamageText = ChatFormatting.GREEN + " +" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
            } else if (additionalDamage < 0) {
                additionalDamageText = ChatFormatting.RED + " " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
            }
        }

        var damage = GunModifierHelper.getModifiedDamage(gunData);
        damage = GunEnchantmentHelper.getAcceleratorDamage(gunData.gun, damage);
        tooltip.add(Component.translatable("info.ntgl.damage",
                ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText).withStyle(ChatFormatting.GRAY));
    }

//    @Override
//    public boolean isBarVisible(ItemStack stack) {
//        CompoundTag tagCompound = stack.getOrCreateTag();
//        Gun modifiedGun = this.getModifiedGun(stack);
//        return !tagCompound.getBoolean("IgnoreAmmo") && tagCompound.getInt(Tags.AMMO_COUNT) != GunEnchantmentHelper.getAmmoCapacity(stack, modifiedGun);
//    }

//    @Override
//    public int getBarWidth(ItemStack stack) {
//        CompoundTag tagCompound = stack.getOrCreateTag();
//        Gun modifiedGun = this.getModifiedGun(stack);
//        return (int) (13.0 * (tagCompound.getInt(Tags.AMMO_COUNT) / (double) GunEnchantmentHelper.getAmmoCapacity(stack, modifiedGun)));
//    }

    public Gun getModifiedGun(ItemStack stack) {
        var tagCompound = stack.getTag();
        if (tagCompound != null && tagCompound.contains("Gun", Tag.TAG_COMPOUND)) {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item ->
            {
                if (tagCompound.getBoolean("Custom")) {
                    var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    return Gun.create(key, tagCompound.getCompound("Gun"));
                } else {
                    var gunCopy = this.gun.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Gun"));
                    return gunCopy;
                }
            });
        }
        if (Ntgl.isDebugging()) {
            return Debug.getGun(this);
        }

        return this.gun;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return this.gun.getGeneral().isEnchantable() && super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (this.gun.getGeneral().isEnchantable()) {
            var data = new GunData(stack, null);
            if (enchantment.category == EnchantmentTypes.SEMI_AUTO_GUN) {
                return GunModifierHelper.isAuto(data);
            }
            return super.canApplyAtEnchantingTable(stack, enchantment);
        }
        else return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

//    @Override
//    public int getBarColor(ItemStack stack) {
//        return requireNonNull(ChatFormatting.YELLOW.getColor());
//    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return this.gun.getGeneral().isEnchantable() && this.getMaxStackSize(stack) == 1;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return this.gun.getGeneral().isEnchantable() ? 5 : 0;
    }

    @Override
    public int getEnchantmentValue() {
        return this.gun.getGeneral().isEnchantable() ? 5 : 0;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private ResourceLocation getRegistryName() {
        return ForgeRegistries.ITEMS.getKey(this);
    }


}