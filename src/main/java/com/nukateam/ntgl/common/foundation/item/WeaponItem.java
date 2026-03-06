package com.nukateam.ntgl.common.foundation.item;

import com.google.common.collect.HashMultimap;
import com.nukateam.geo.render.ProxyItemRenderer;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableItemEntity;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.geo.interfaces.DynamicGeoItem;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.ntgl.client.render.renderers.weapon.*;
import com.nukateam.ntgl.common.foundation.item.interfaces.*;
import net.minecraft.*;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.*;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import javax.annotation.*;
import java.util.*;
import java.util.function.*;

import static software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache;

public class WeaponItem extends Item implements DynamicGeoItem, IWeapon, IThrowable{
    public static final String VARIANT = "variant";
    private final Lazy<ResourceLocation> id = Lazy.of(this::getRegistryName);
    private final WeakHashMap<CompoundTag, WeaponConfig> modifiedGunCache = new WeakHashMap<>();
    private final Lazy<DefaultWeaponRendererGeo> WEAPON_RENDERER = Lazy.of(() -> new DefaultWeaponRendererGeo());
    private WeaponConfig weaponConfig = new WeaponConfig();

    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    protected IWeaponModifier[] modifiers;

    public WeaponItem(Item.Properties properties, IWeaponModifier... modifiers) {
        super(properties);
        this.modifiers = modifiers;
    }

    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public IWeaponModifier[] getModifiers() {
        return modifiers;
    }

    @OnlyIn(Dist.CLIENT)
    public DynamicGeoItemRenderer getRenderer() {
        return WEAPON_RENDERER.get();
    }

    @Override
    public BiFunction<ItemDisplayContext, DynamicWeaponRenderer<WeaponAnimator>, WeaponAnimator> getAnimatorFactory() {
        return WeaponAnimator::new;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public void setConfig(ConfigSupplier<WeaponConfig> supplier) {
        this.weaponConfig = supplier.config();
        weaponConfig.onCreated(getId().getPath());
    }

    @Override
    public WeaponConfig getConfig() {
        return this.weaponConfig;
    }

    @Override
    public ResourceLocation getId() {
        return id.get();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ProxyItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new ProxyItemRenderer(getRenderer());

                return this.renderer;
            }
        });
    }

    public static String getVariant(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains(VARIANT, Tag.TAG_STRING)) {
            tag.putString(VARIANT, "default");
        }

        return tag.getString(VARIANT);
    }

    public static Map<UUID, HashMultimap<Attribute, AttributeModifier>> PLAYER_MODIFIERS = new HashMap<>();

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if(entity instanceof LivingEntity livingEntity) {
            WeaponItemUtils.checkAmmo(stack, entity, livingEntity);

            if(isItemInHands(stack, livingEntity)) {
                var mods = PLAYER_MODIFIERS.get(livingEntity.getUUID());
                if (mods != null) {
                    livingEntity.getAttributes().removeAttributeModifiers(mods);
                }
                WeaponItemUtils.applyAttributeModifiers(livingEntity, InteractionHand.MAIN_HAND);
                WeaponItemUtils.applyAttributeModifiers(livingEntity, InteractionHand.OFF_HAND);
            }
        }
    }

    private static boolean isItemInHands(ItemStack stack, LivingEntity livingEntity) {
        return stack == livingEntity.getItemInHand(InteractionHand.MAIN_HAND) ||
                stack == livingEntity.getItemInHand(InteractionHand.OFF_HAND);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        var data = new WeaponData(stack, null);
        var tagCompound = stack.getOrCreateTag();
        WeaponItemTooltips.addAmmoType(tooltip, data);
        WeaponItemTooltips.addFireRate(tooltip, data);
        WeaponItemTooltips.addDamage(tooltip, data);
        WeaponItemTooltips.addMelleDamage(tooltip, data);

        var explosion = WeaponStateHelper.getProjectileConfig(data).getExplosion();
        if(explosion.getRadius() > 0){
            WeaponItemTooltips.addExplosionTip(tooltip, explosion);
        }

        WeaponItemTooltips.addAmmo(tooltip, tagCompound, data);
        WeaponItemTooltips.addFuel(tooltip, data);

        var name = NtglKeyBinds.KEY_ATTACHMENTS.getKey().getDisplayName();

        tooltip.add(Component.translatable("info.ntgl.attachment_help", name)
         .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public WeaponConfig getModifiedConfig(ItemStack stack) {
        var tagCompound = stack.getTag();
        if (tagCompound != null && tagCompound.contains("Gun", Tag.TAG_COMPOUND)) {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item ->
            {
                if (tagCompound.getBoolean("Custom")) {
                    var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    return WeaponConfig.create(key, tagCompound.getCompound("Gun"));
                } else {
                    var gunCopy = this.weaponConfig.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Gun"));
                    return gunCopy;
                }
            });
        }

        return this.weaponConfig;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return this.weaponConfig.getGeneral().isEnchantable() && super.isBookEnchantable(stack, book);
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
    public boolean isEnchantable(ItemStack stack) {
        return this.weaponConfig.getGeneral().isEnchantable() && this.getMaxStackSize(stack) == 1;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return this.weaponConfig.getGeneral().isEnchantable() ? 5 : 0;
    }

    @Override
    public int getEnchantmentValue() {
        return this.weaponConfig.getGeneral().isEnchantable() ? 5 : 0;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private ResourceLocation getRegistryName() {
        return ForgeRegistries.ITEMS.getKey(this);
    }

    @Override
    public void expire(LivingEntity entityLiving) {
        var throwableEntity = this.createThrowable(entityLiving.level(), entityLiving, 0);
        throwableEntity.onDeath();
    }

    @Override
    public void throwItem(ItemStack stack, LivingEntity entityLiving, int timeLeft) {
        var level = entityLiving.level();

        if (!(entityLiving instanceof Player player) || !player.isCreative()) {
            stack.shrink(1);
        }

        var grenade = this.createThrowable(level, entityLiving, timeLeft);
        grenade.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, Math.min(1.0F, timeLeft / 20F), 1.0F);
        level.addFreshEntity(grenade);
        this.onThrown(level, grenade);

        if (entityLiving instanceof Player) {
            ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
        }
    }

    protected void onThrown(Level world, ThrowableItemEntity entity) {}

    protected ThrowableItemEntity createThrowable(Level world, LivingEntity entity, int timeLeft) {
        var projectile = getConfig().getThrowable().getProjectile().getProjectileType();
        return ProjectileManager.getInstance()
                .getFactory(projectile)
                .create(world, entity, this, timeLeft);
    }
}