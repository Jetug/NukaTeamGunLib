package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.geo.render.ProxyItemRenderer;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.ExplosionConfig;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
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

import java.util.*;
import java.util.function.*;
import net.neoforged.neoforge.common.util.Lazy;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

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

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if(entity instanceof LivingEntity livingEntity) {
            var data = new WeaponData(stack, livingEntity);
            var ammoItems = WeaponModifierHelper.getAmmoItems(data);
            var ammo = WeaponStateHelper.getCurrentAmmo(data);
            var matches = ammoItems.stream().anyMatch((i) -> i.equals(ammo));

            if(!matches) {
                if (entity instanceof ServerPlayer player) {
                    ServerPlayHandler.unloadGun(new WeaponData(stack, player));
                }
                var firstAmmo = SetUtils.getFirst(ammoItems);
                WeaponStateHelper.setCurrentAmmo(data, firstAmmo.getId());
            }

            var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
            var currentAmount = WeaponStateHelper.getAmmoCount(data);
            if(currentAmount > maxAmmo){
                if (entity instanceof ServerPlayer player) {
                    ServerPlayHandler.unloadGun(new WeaponData(stack, player));
                }
            }
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var data = new WeaponData(stack, null);
        addAmmoType(tooltip, data);
        addFireRate(tooltip, data);
        addDamage(tooltip, data);
        addMelleDamage(tooltip, data);

        var explosion = WeaponStateHelper.getProjectileConfig(data).getExplosion();
        if(explosion.getRadius() > 0){
            addExplosionTip(tooltip, explosion);
        }

        addAmmo(tooltip, data);
        addFuel(tooltip, data);

        tooltip.add(Component.translatable("info.ntgl.attachment_help", Component.keybind("key.ntgl.attachments")
                        .getString().toUpperCase(Locale.ENGLISH))
                .withStyle(ChatFormatting.YELLOW));
    }

    public static void addExplosionTip(List<Component> tooltip, ExplosionConfig explosion) {
        var damage = explosion.getDamage();
        tooltip.add(Component.translatable("info.ntgl.explosionDamage",
                        ChatFormatting.WHITE + ATTRIBUTE_MODIFIER_FORMAT.format(damage))
                .withStyle(ChatFormatting.GRAY));

        var radius = explosion.getRadius();
        tooltip.add(Component.translatable("info.ntgl.explosionRadius",
                        ChatFormatting.WHITE + ATTRIBUTE_MODIFIER_FORMAT.format(radius))
                .withStyle(ChatFormatting.GRAY));
    }

    private static void addAmmoType(List<Component> tooltip, WeaponData data) {
        var descriptionId = WeaponStateHelper.getCurrentAmmo(data).getDescriptionId();

        tooltip.add(Component.translatable("info.ntgl.ammo_type",
                Component.translatable(descriptionId).withStyle(ChatFormatting.WHITE)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addFireRate(List<Component> tooltip, WeaponData data) {
        var rate = WeaponModifierHelper.getRate(data);
        rate = rate == 0 ? 0 : 20 / rate;

        tooltip.add(Component.translatable("info.ntgl.rate",
                        ChatFormatting.WHITE + ATTRIBUTE_MODIFIER_FORMAT.format(rate))
                .withStyle(ChatFormatting.GRAY));
    }

    private static void addFuel(List<Component> tooltip, WeaponData weaponData) {
        var allFuel = WeaponModifierHelper.getAllFuel(weaponData);
        for (var fuelType : allFuel) {
            var fuelAmount = FuelUtils.getFuel(weaponData.weapon, fuelType);
            var maxFuel = WeaponModifierHelper.getMaxFuel(fuelType.getId(), weaponData);

            tooltip.add(Component.translatable(fuelType.getDescriptionId())
                    .append(ChatFormatting.WHITE + " : " + fuelAmount + "/" + maxFuel)
                    .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private static void addAmmo(List<Component> tooltip, WeaponData weaponData) {
        if (WeaponStateHelper.isAmmoIgnored(weaponData)) {
            tooltip.add(Component.translatable("info.ntgl.ignore_ammo").withStyle(ChatFormatting.AQUA));
        } else {
            int ammoCount = WeaponStateHelper.getAmmoCount(weaponData);
            tooltip.add(Component.translatable("info.ntgl.ammo",
                    ChatFormatting.WHITE.toString()
                            + ammoCount + "/"
                            + WeaponModifierHelper.getMaxAmmo(weaponData)).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addDamage(List<Component> tooltip, WeaponData weaponData) {
        var damage = WeaponStateHelper.getProjectileDamage(weaponData);
        tooltip.add(Component.translatable("info.ntgl.damage", ChatFormatting.WHITE
                + ATTRIBUTE_MODIFIER_FORMAT.format(damage)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addMelleDamage(List<Component> tooltip, WeaponData weaponData) {
        var damage = WeaponModifierHelper.getMeleeDamage(weaponData);

        tooltip.add(Component.translatable("info.ntgl.melee_damage",
                ChatFormatting.WHITE + ATTRIBUTE_MODIFIER_FORMAT.format(damage)
        ).withStyle(ChatFormatting.GRAY));
    }

    public WeaponConfig getModifiedConfig(ItemStack stack) {
        var tagCompound = NtglComponents.getWeaponTag(stack);
        if (tagCompound.contains("Gun", Tag.TAG_COMPOUND)) {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item ->
            {
                if (tagCompound.getBoolean("Custom")) {
                    var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    return WeaponConfig.create(key, tagCompound.getCompound("Gun"));
                } else {
                    var gunCopy = this.weaponConfig.copy();
                    gunCopy.deserializeNBT(null, tagCompound.getCompound("Gun"));
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}