package com.nukateam.gunscore.common.foundation.item;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.example.common.data.utils.ResourceUtils;
import com.nukateam.gunscore.client.render.renderers.GunItemRenderer;
import com.nukateam.gunscore.common.base.gun.Gun;
import com.nukateam.gunscore.common.base.NetworkGunManager;
import com.nukateam.gunscore.common.data.constants.Tags;
import com.nukateam.gunscore.common.data.util.GunEnchantmentHelper;
import com.nukateam.gunscore.common.data.util.GunModifierHelper;
import com.nukateam.gunscore.common.debug.Debug;
import com.nukateam.gunscore.common.foundation.enchantment.EnchantmentTypes;
import com.nukateam.gunscore.GunMod;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import net.minecraft.*;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class GunItem extends Item implements GeoItem, IColored, IMeta, IResourceProvider {
//    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    private final Lazy<String> name = Lazy.of(() -> ResourceUtils.getResourceName(getRegistryName()));
    private final WeakHashMap<CompoundTag, Gun> modifiedGunCache = new WeakHashMap<>();

    private Gun gun = new Gun();


    public GunItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    public void setGun(NetworkGunManager.Supplier supplier) {
        this.gun = supplier.getGun();
    }

    public Gun getGun() {
        return this.gun;
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public String getNamespace() {
        return getRegistryName().getNamespace();
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            private GunItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                if (renderer == null)
                    return new GunItemRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        var modifiedGun = this.getModifiedGun(stack);
        var ammo = ForgeRegistries.ITEMS.getValue(modifiedGun.getProjectile().getItem());

        if (ammo != null) {
            tooltip.add(new TranslatableComponent("info.gunscore.ammo_type", new TranslatableComponent(ammo.getDescriptionId()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
        }

        var additionalDamageText = "";
        var tagCompound = stack.getTag();
        if (tagCompound != null) {
            if (tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC)) {
                float additionalDamage = tagCompound.getFloat("AdditionalDamage");
                additionalDamage += GunModifierHelper.getAdditionalDamage(stack);

                if (additionalDamage > 0) {
                    additionalDamageText = ChatFormatting.GREEN + " +" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                } else if (additionalDamage < 0) {
                    additionalDamageText = ChatFormatting.RED + " " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                }
            }
        }

        float damage = modifiedGun.getProjectile().getDamage();
        damage = GunModifierHelper.getModifiedProjectileDamage(stack, damage);
        damage = GunEnchantmentHelper.getAcceleratorDamage(stack, damage);
        tooltip.add(new TranslatableComponent("info.gunscore.damage",
                ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText).withStyle(ChatFormatting.GRAY));

        if (tagCompound != null) {
            if (tagCompound.getBoolean("IgnoreAmmo")) {
                tooltip.add(new TranslatableComponent("info.gunscore.ignore_ammo").withStyle(ChatFormatting.AQUA));
            } else {
                int ammoCount = tagCompound.getInt(Tags.AMMO_COUNT);
                tooltip.add(new TranslatableComponent("info.gunscore.ammo",
                        ChatFormatting.WHITE.toString() + ammoCount + "/" + GunEnchantmentHelper.getAmmoCapacity(stack, modifiedGun)).withStyle(ChatFormatting.GRAY));
            }
        }
        //tooltip.add(new TranslatableComponent("info.gunscore.attachment_help", new KeybindComponent("key.gunscore.attachments").getString().toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks) {
        if (this.allowdedIn(group)) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putInt(Tags.AMMO_COUNT, this.gun.getGeneral().getMaxAmmo(stack));
            stacks.add(stack);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        Gun modifiedGun = this.getModifiedGun(stack);
        return !tagCompound.getBoolean("IgnoreAmmo") && tagCompound.getInt(Tags.AMMO_COUNT) != GunEnchantmentHelper.getAmmoCapacity(stack, modifiedGun);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        Gun modifiedGun = this.getModifiedGun(stack);
        return (int) (13.0 * (tagCompound.getInt(Tags.AMMO_COUNT) / (double) GunEnchantmentHelper.getAmmoCapacity(stack, modifiedGun)));
    }

    public Gun getModifiedGun(ItemStack stack) {
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound != null && tagCompound.contains("Gun", Tag.TAG_COMPOUND)) {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item ->
            {
                if (tagCompound.getBoolean("Custom")) {
                    return Gun.create(tagCompound.getCompound("Gun"));
                } else {
                    Gun gunCopy = this.gun.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Gun"));
                    return gunCopy;
                }
            });
        }
        if (GunMod.isDebugging()) {
            return Debug.getGun(this);
        }

        return this.gun;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.category == EnchantmentTypes.SEMI_AUTO_GUN) {
            Gun modifiedGun = this.getModifiedGun(stack);
            return !modifiedGun.getGeneral().isAuto();
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return requireNonNull(ChatFormatting.YELLOW.getColor());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return this.getItemStackLimit(stack) == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 5;
    }

    public static final Map<ItemStack, String> stackAnimations = new HashMap<>();

    public static void doAnim(ItemStack stack, String animation) {
        stackAnimations.put(stack, animation);
    }

    public static void resetAnim(ItemStack stack) {
        stackAnimations.put(stack, null);
    }

    protected final AnimatableInstanceCache cache = createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
