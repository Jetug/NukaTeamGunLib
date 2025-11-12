package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.geo.render.ProxyItemRenderer;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.ExplosionConfig;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableItemEntity;
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
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.*;
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

import static com.nukateam.ntgl.common.data.constants.Tags.AMMO_COUNT;
import static com.nukateam.ntgl.common.util.util.WeaponStateHelper.AMMO_TAG;
import static software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache;

public class WeaponItem extends Item implements DynamicGeoItem, IWeapon, IThrowable, IColored{
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

    public void setDefaultTag(CompoundTag tag){
        tag.putInt(AMMO_COUNT, getConfig().getGeneral().getMaxAmmo());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if(entity instanceof LivingEntity livingEntity) {
            var tag = stack.getOrCreateTag();
            var data = new WeaponData(stack, livingEntity);
            var ammoItems = WeaponModifierHelper.getAmmoItems(data);

            WeaponStateHelper.getCurrentAmmo(data);

            if (tag.contains(AMMO_TAG, Tag.TAG_STRING)) {
                var ammoId = tag.getString(AMMO_TAG);
                var matches = ammoItems.stream().anyMatch((i) -> i.getId().toString().equals(ammoId));

                if(!matches) {
                    if (entity instanceof ServerPlayer player) {
                        ServerPlayHandler.unloadGun(player, stack);
                    }
                    var firstAmmo = SetUtils.getFirst(ammoItems);
                    WeaponStateHelper.setCurrentAmmo(data, firstAmmo.getId());
                }
            }

            var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
            var currentAmount = WeaponStateHelper.getAmmoCount(data);
            if(currentAmount > maxAmmo){
                if (entity instanceof ServerPlayer player) {
                    ServerPlayHandler.unloadGun(player, stack);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        var data = new WeaponData(stack, null);
        var tagCompound = stack.getOrCreateTag();
        addAmmoType(tooltip, data);
        addFireRate(tooltip, data);
        addDamage(tooltip, tagCompound, data);
        addMelleDamage(tooltip, data);

        var explosion = WeaponStateHelper.getProjectileConfig(data).getExplosion();
        if(explosion.getRadius() > 0){
            addExplosionTip(tooltip, explosion);
        }

        addAmmo(tooltip, tagCompound, data);
        addFuel(tooltip, data);


        var name = NtglKeyBinds.KEY_ATTACHMENTS.getKey().getDisplayName();

        tooltip.add(Component.translatable("info.ntgl.attachment_help", name)
         .withStyle(ChatFormatting.YELLOW));


//        Component.keybind("key.ntgl.attachments").getString().toUpperCase(Locale.ENGLISH))
    }

    public static void addExplosionTip(List<Component> tooltip, ExplosionConfig explosion) {
        var damage = explosion.getDamage();
        tooltip.add(Component.translatable("info.ntgl.explosionDamage",
                        ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage))
                .withStyle(ChatFormatting.GRAY));

        var radius = explosion.getRadius();
        tooltip.add(Component.translatable("info.ntgl.explosionRadius",
                        ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(radius))
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
                ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rate))
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

    private static void addAmmo(List<Component> tooltip, CompoundTag tagCompound, WeaponData weaponData) {
        if (tagCompound.getBoolean("IgnoreAmmo")) {
            tooltip.add(Component.translatable("info.ntgl.ignore_ammo").withStyle(ChatFormatting.AQUA));
        } else {
            int ammoCount = tagCompound.getInt(AMMO_COUNT);
            tooltip.add(Component.translatable("info.ntgl.ammo",
                    ChatFormatting.WHITE.toString()
                            + ammoCount + "/"
                            + WeaponModifierHelper.getMaxAmmo(weaponData)).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addDamage(List<Component> tooltip, CompoundTag tagCompound, WeaponData weaponData) {
        var damage = WeaponStateHelper.getProjectileDamage(weaponData);
        tooltip.add(Component.translatable("info.ntgl.damage", ChatFormatting.WHITE
                        + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)
        ).withStyle(ChatFormatting.GRAY));
    }

    private static void addMelleDamage(List<Component> tooltip, WeaponData weaponData) {
        var damage = WeaponModifierHelper.getMeleeDamage(weaponData);

        tooltip.add(Component.translatable("info.ntgl.melee_damage",
                ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)
        ).withStyle(ChatFormatting.GRAY));
    }

//    @Override
//    public boolean isBarVisible(ItemStack stack) {
//        CompoundTag tagCompound = stack.getOrCreateTag();
//        Gun modifiedGun = this.getModifiedConfig(stack);
//        return !tagCompound.getBoolean("IgnoreAmmo") && tagCompound.getInt(Tags.AMMO_COUNT) != WeaponModifierHelper.getMaxAmmo(stack, modifiedGun);
//    }

//    @Override
//    public int getBarWidth(ItemStack stack) {
//        CompoundTag tagCompound = stack.getOrCreateTag();
//        Gun modifiedGun = this.getModifiedConfig(stack);
//        return (int) (13.0 * (tagCompound.getInt(Tags.AMMO_COUNT) / (double) WeaponModifierHelper.getMaxAmmo(stack, modifiedGun)));
//    }

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
//        if (Ntgl.isDebugging()) {
//            return Debug.getGun(this);
//        }

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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

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