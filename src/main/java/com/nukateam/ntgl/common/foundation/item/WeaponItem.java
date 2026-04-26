package com.nukateam.ntgl.common.foundation.item;

import com.google.common.collect.HashMultimap;
import com.nukateam.geo.render.ProxyItemRenderer;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.ExplosionConfig;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableItemEntity;
import com.nukateam.ntgl.common.foundation.init.NtglComponents;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.common.util.util.FuelUtils;
import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.geo.interfaces.DynamicGeoItem;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.ntgl.client.render.renderers.weapon.*;
import com.nukateam.ntgl.common.foundation.item.interfaces.*;
import net.minecraft.*;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.*;
import net.neoforged.neoforge.common.util.Lazy;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

import static software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache;
import static net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT;

public class WeaponItem extends Item implements DynamicGeoItem, IWeapon, IThrowable{
    public static final String VARIANT = "variant";
    private final Lazy<ResourceLocation> id = Lazy.of(this::getRegistryName);
    private final WeakHashMap<CompoundTag, WeaponConfig> modifiedGunCache = new WeakHashMap<>();
    private final Lazy<DefaultWeaponRendererGeo> WEAPON_RENDERER = Lazy.of(() -> new DefaultWeaponRendererGeo());
    private WeaponConfig weaponConfig = new WeaponConfig();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected IWeaponModifier[] modifiers;

    public WeaponItem(Item.Properties properties, IWeaponModifier... modifiers) {
        super(properties);
        this.modifiers = modifiers;
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

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

//    @Override
//    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
//        consumer.accept(new IClientItemExtensions() {
//            private ProxyItemRenderer renderer;
//
//            @Override
//            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//                if (this.renderer == null)
//                    this.renderer = new ProxyItemRenderer(getRenderer());
//
//                return this.renderer;
//            }
//        });
//    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private ProxyItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new ProxyItemRenderer(getRenderer());

                return this.renderer;
            }
        });
    }

    public void setDefaultTag(ItemStack stack){
        WeaponStateHelper.setAmmoCount(new WeaponData(stack, null), getConfig().getGeneral().getMaxAmmo());
    }

    public static Map<UUID, HashMultimap<Holder<Attribute>, AttributeModifier>> PLAYER_MODIFIERS = new HashMap<>();

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if(entity instanceof LivingEntity livingEntity) {
            WeaponItemUtils.checkAmmo(stack, entity, livingEntity);

            if(isItemInHands(stack, livingEntity)) {
                var mods = PLAYER_MODIFIERS.get(livingEntity.getUUID());
                if (mods != null) {
                    livingEntity.getAttributes().removeAttributeModifiers(mods);
                }
                applyAttributeModifiers(livingEntity, InteractionHand.MAIN_HAND);
                applyAttributeModifiers(livingEntity, InteractionHand.OFF_HAND);
            }
        }
    }

    public static void applyAttributeModifiers(LivingEntity player, InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() instanceof IWeapon) {
            var modifiers = WeaponModifierHelper.getAttributeModifiers(new WeaponData(heldItem, player));
            var multiMap = HashMultimap.<Holder<Attribute>, AttributeModifier>create();

            for (var modifier : modifiers) {
                BuiltInRegistries.ATTRIBUTE.getHolder(modifier.getAttribute()).ifPresent((attributeHolder) -> {
                    var attributeInstance = player.getAttribute(attributeHolder);
                    if (attributeInstance != null) {
                        var name = modifier.getAttribute().toString().replace(".", "_") + "_" + hand.toString().toLowerCase(Locale.ROOT);
                        var id = ResourceLocation.parse(name);
                        var newModifier = new AttributeModifier(id, modifier.getValue(), modifier.getOperation());

                        if (!attributeInstance.hasModifier(id)) {
                            attributeInstance.addTransientModifier(newModifier);
                        }
                        multiMap.put(attributeHolder, newModifier);
                    }
                });
            }
            WeaponItem.PLAYER_MODIFIERS.put(player.getUUID(), multiMap);
        }
    }

    private static boolean isItemInHands(ItemStack stack, LivingEntity livingEntity) {
        return stack == livingEntity.getItemInHand(InteractionHand.MAIN_HAND) ||
                stack == livingEntity.getItemInHand(InteractionHand.OFF_HAND);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        var data = new WeaponData(stack, null);
        WeaponItemTooltips.addAmmoType(tooltip, data);
        WeaponItemTooltips.addFireRate(tooltip, data);
        WeaponItemTooltips.addDamage(tooltip, data);
        WeaponItemTooltips.addMelleDamage(tooltip, data);

        var explosion = WeaponStateHelper.getProjectileConfig(data).getExplosion();
        if(explosion.getRadius() > 0){
            WeaponItemTooltips.addExplosionTip(tooltip, explosion);
        }

        WeaponItemTooltips.addAmmo(tooltip, data);
        WeaponItemTooltips.addFuel(tooltip, data);

        var name = NtglKeyBinds.KEY_ATTACHMENTS.getKey().getDisplayName();

        tooltip.add(Component.translatable("info.ntgl.attachment_help", name)
         .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public WeaponConfig getModifiedConfig(ItemStack stack) {
        var tag = NtglComponents.getWeaponTag(stack);
        if (tag.contains("Gun", Tag.TAG_COMPOUND)) {
            return this.modifiedGunCache.computeIfAbsent(tag, item ->
            {
                if (tag.getBoolean("Custom")) {
                    var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    return WeaponConfig.create(key, tag.getCompound("Gun"));
                } else {
                    var gunCopy = this.weaponConfig.copy();
                    gunCopy.deserializeNBT(null, tag.getCompound("Gun"));
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
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
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

    private ResourceLocation getRegistryName() {
        return BuiltInRegistries.ITEM.getKey(this);
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

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}