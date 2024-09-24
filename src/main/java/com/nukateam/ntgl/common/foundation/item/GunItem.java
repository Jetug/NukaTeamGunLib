package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.example.common.data.utils.ResourceUtils;
import com.nukateam.ntgl.client.render.renderers.DefaultGunRenderer;
import com.nukateam.ntgl.client.render.renderers.DynamicGunRenderer;
import com.nukateam.ntgl.client.render.renderers.GunItemRenderer;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.base.NetworkGunManager;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.util.GunEnchantmentHelper;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.foundation.enchantment.EnchantmentTypes;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import com.nukateam.ntgl.common.foundation.item.interfaces.IMeta;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import net.minecraft.*;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class GunItem extends Item implements GeoItem, IColored, IMeta, IResourceProvider {
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    private final Lazy<String> name = Lazy.of(() -> ResourceUtils.getResourceName(getRegistryName()));
    private final WeakHashMap<CompoundTag, Gun> modifiedGunCache = new WeakHashMap<>();
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private final Lazy<DefaultGunRenderer> GUN_RENDERER = Lazy.of(() -> new DefaultGunRenderer());

    private Gun gun = new Gun();

    public GunItem(Item.Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    public DynamicGunRenderer getRenderer(){
        return GUN_RENDERER.get();
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

    private ResourceLocation getRegistryName() {
        return ForgeRegistries.ITEMS.getKey(this);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GunItemRenderer renderer = null;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null)
                    return new GunItemRenderer(getRenderer());
                return this.renderer;
            }
        });
    }

//    @Override
//    public void inventoryTick(ItemStack stack, Level pLevel, Entity entity, int pSlotId, boolean pIsSelected) {
//        checkAmmoCount(stack, entity);
//
//        super.inventoryTick(stack, pLevel, entity, pSlotId, pIsSelected);
//    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        var modifiedGun = this.getModifiedGun(stack);
        var ammo = ForgeRegistries.ITEMS.getValue(GunModifierHelper.getAmmoItem(stack));

        if (ammo != null) {
            tooltip.add(Component.translatable("info.ntgl.ammo_type", Component.translatable(ammo.getDescriptionId()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
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

        float damage = GunModifierHelper.getCurrentProjectile(stack).getDamage();
        damage = GunModifierHelper.getModifiedProjectileDamage(stack, damage);
        damage = GunEnchantmentHelper.getAcceleratorDamage(stack, damage);
        tooltip.add(Component.translatable("info.ntgl.damage",
                ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText).withStyle(ChatFormatting.GRAY));

        if (tagCompound != null) {
            if (tagCompound.getBoolean("IgnoreAmmo")) {
                tooltip.add(Component.translatable("info.ntgl.ignore_ammo").withStyle(ChatFormatting.AQUA));
            } else {
                int ammoCount = tagCompound.getInt(Tags.AMMO_COUNT);
                tooltip.add(Component.translatable("info.ntgl.ammo", ChatFormatting.WHITE.toString() + ammoCount + "/" + GunEnchantmentHelper.getAmmoCapacity(stack)).withStyle(ChatFormatting.GRAY));
            }
        }
        //tooltip.add(Component.translatable("info.ntgl.attachment_help", new KeybindComponent("key.ntgl.attachments").getString().toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.YELLOW));
    }

//    @Override
//    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks) {
//        if (this.allowedIn(group)) {
//            ItemStack stack = new ItemStack(this);
//            stack.getOrCreateTag().putInt(Tags.AMMO_COUNT, this.gun.getGeneral().getMaxAmmo(stack));
//            stacks.add(stack);
//        }
//    }

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
                    return Gun.create(tagCompound.getCompound("Gun"));
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
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.category == EnchantmentTypes.SEMI_AUTO_GUN) {
            return GunModifierHelper.isAuto(stack);
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

//    @Override
//    public int getBarColor(ItemStack stack) {
//        return requireNonNull(ChatFormatting.YELLOW.getColor());
//    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return this.getMaxStackSize(stack) == 1;
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


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
