package com.nukateam.chassis_core.common.foundation.entity;

import com.nukateam.chassis_core.client.render.utils.GeoUtils;
import com.nukateam.chassis_core.client.render.utils.ResourceHelper;
import com.nukateam.chassis_core.common.config.ChassisConfig;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.chassis_core.common.data.holders.*;
import com.nukateam.chassis_core.common.events.ContainerChangedEvent;
import com.nukateam.chassis_core.common.foundation.container.menu.DynamicChassisMenu;
import com.nukateam.chassis_core.common.foundation.entity.EmptyLivingEntity;
import com.nukateam.chassis_core.common.foundation.item.ChassisArmor;
import com.nukateam.chassis_core.common.foundation.item.ChassisEquipment;
import com.nukateam.chassis_core.common.foundation.item.ItemStackUtils;
import com.nukateam.chassis_core.common.foundation.item.StackUtils;
import com.nukateam.chassis_core.common.network.PacketHandler;
import com.nukateam.chassis_core.common.network.managers.ConfigSupplier;
import com.nukateam.chassis_core.common.network.managers.Configs;
import com.nukateam.chassis_core.common.network.packet.S2CInventoryPacket;
import com.nukateam.chassis_core.common.util.helpers.timer.TickTimer;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Lazy;
import net.neoforged.neoforge.registries.Registries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static com.nukateam.chassis_core.common.data.constants.NBT.ITEMS_TAG;
import static com.nukateam.chassis_core.common.util.extensions.Collection.arrayListOf;
import static com.nukateam.chassis_core.common.util.helpers.ContainerUtils.copyContainer;
import static com.nukateam.chassis_core.common.util.helpers.ContainerUtils.isContainersEqual;
import static com.nukateam.chassis_core.common.util.helpers.InventoryHelper.deserializeInventory;
import static com.nukateam.chassis_core.common.util.helpers.InventoryHelper.serializeInventory;
import static java.util.Arrays.stream;
import static java.util.Collections.addAll;

public class Chassis extends EmptyLivingEntity implements ContainerListener {
    protected HashMap<ChassisPart, Integer> partIdMap = new HashMap<>();

    protected final TickTimer timer = new TickTimer();
    protected final boolean isClientSide = level().isClientSide;
    protected final boolean isServerSide = !level().isClientSide;
    private final Lazy<String> chassisId = Lazy.of(() -> ResourceHelper.getResourceName(Registries.ENTITY_TYPE.getKey(this.getType())));

    public final HashMap<String, ArrayList<GeoBone>> attachmentForBone = new HashMap<>();
    public final HashMap<String, ResourceLocation> textureForBone = new HashMap<>();

    public Collection<String> bonesToHide = new ArrayList<>();
    public SimpleContainer inventory;
    protected float totalDefense;
    protected float totalToughness;
    private int inventorySize = 0;

    private ListTag serializedInventory;
    private Container previousContainer;
    private int tickTimer = 10;
    private int tickTimer5 = 5;

    public Chassis(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        noCulling = true;
        initInventory();
        updateParams();
        loadSlots();
    }

//    public int updateInventorySize(){
//        loadSlots();
//        return this.inventorySize;
//    }

    private void loadSlots() {
        var i = 0;
        for (var part : getConfig().getParts()) {
            partIdMap.put(part, i++);
        }
        this.inventorySize = partIdMap.size();
    }

    public static ChassisEquipment getAsChassisEquipment(ItemStack itemStack) {
        return (ChassisEquipment) itemStack.getItem();
    }

    public static <T, R> Collection<R> returnCollection(Collection<T> collection, Function<T, R> function) {
        var result = new ArrayList<R>();
        for (var item : collection) {
            var val = function.apply(item);
            if (val != null) result.add(val);
        }
        return result;
    }

    public <S extends INBTSerializable<CompoundTag>> void setConfig(ConfigSupplier<S> sConfigSupplier) {}

    //GETTERS
    public ChassisConfig getConfig(){
        return Configs.CHASSIS_CONFIGS.get(this.getType()).getConfig();
    }

    public float getTotalDefense() {
        return totalDefense;
    }

    public float getTotalToughness() {
        return totalToughness;
    }

    public Collection<String> getBonesToHide() {
        return bonesToHide;
    }

    public int getInventorySize() {
        return inventorySize;
    }

    public void damageArmor(DamageSource damageSource, float damage) {
        if (isServerSide) {
            if (!damageSource.is(DamageTypes.FALL)) {
                damageArmorItem(ChassisPart.HELMET, damageSource, damage);
                damageArmorItem(ChassisPart.BODY_ARMOR, damageSource, damage);
                damageArmorItem(ChassisPart.LEFT_ARM_ARMOR, damageSource, damage);
                damageArmorItem(ChassisPart.RIGHT_ARM_ARMOR, damageSource, damage);
            }
            damageArmorItem(ChassisPart.LEFT_LEG_ARMOR, damageSource, damage);
            damageArmorItem(ChassisPart.RIGHT_LEG_ARMOR, damageSource, damage);
        }
    }

    public void damageArmorItem(ChassisPart chassisPart, DamageSource damageSource, float damage) {
        var itemStack = getEquipment(chassisPart);

        if (itemStack.getItem() instanceof ChassisArmor) {
            ItemStackUtils.damageItem(itemStack, (int)damage);
            setEquipment(chassisPart, itemStack);
        }
    }

    public ArrayList<String> getMods() {
        var res = new ArrayList<String>();
        for (var config : getItemConfigs())
            res.addAll(List.of(config.mods));
        return res;
    }

    public ArrayList<String> getVisibleMods() {
        var res = new ArrayList<String>();
        for (var item : getVisibleEquipment())
            res.addAll(StackUtils.getAttachments(item));
        return res;
    }

    @OnlyIn(Dist.CLIENT)
    public Collection<GeoBone> getAttachmentForBone(String chassisBone) {
        var result = attachmentForBone.get(chassisBone);
        return result == null ? new ArrayList<>() : result;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTextureForBone(String bone) {
        return textureForBone.get(bone);
    }


    @Nullable
    public Integer getPartId(ChassisPart chassisPart) {
        return partIdMap.get(chassisPart);
    }

    @Override
    public void tick() {
        super.tick();

        if(isClientSide) {
            if (tickTimer == 0) {
                tickTimer = 10;
                updateBones();
            } else tickTimer--;
        }
        else {
            tickTimer5 = Math.max(tickTimer5 - 1, 0);

            if(tickTimer5 == 0) {
                syncDataWithClient();
                tickTimer5 = 50;
            }
        }
    }

    @Override
    protected void tickEffects() {
        removeEffects();
    }

    public void removeEffects() {
        for (var effectInstance : this.getActiveEffects()) {
            removeEffect(effectInstance.getEffect());
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        saveInventory(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        loadInventory(compound);
    }

    @Override
    public void containerChanged(@NotNull Container container) {
        if (previousContainer == null || !isContainersEqual(previousContainer, container)) {
            containerReallyChanged(container);
            previousContainer = copyContainer(container);
        }
    }

    public String getModelId() {
        return chassisId.get();
    }

    public void containerReallyChanged(Container container) {
        updateParams();
        serializedInventory = serializeInventory(inventory);
        syncDataWithClient();
        MinecraftForge.EVENT_BUS.post(new ContainerChangedEvent(this));
    }

    public boolean isEquipmentVisible(ChassisPart chassisPart) {
//        if (isArmorItem(chassisPart))
//            return hasArmor(chassisPart);
        return !getEquipment(chassisPart).isEmpty() && hasArmor(chassisPart);
    }

//    public boolean isArmorItem(ChassisPart chassisPart) {
//        return stream(armorParts).toList().contains(chassisPart);
//    }

    public boolean hasArmor(ChassisPart chassisPart) {
        return getArmorDurability(chassisPart) != 0;
    }

    public Collection<ChassisPart> getEquipment() {
        return partIdMap.keySet();
    }

    public Collection<ChassisPart> getPovEquipment() {
        return Collections.singleton(ChassisPart.RIGHT_ARM_ARMOR);
    }

    public boolean hasEquipment(ChassisPart part) {
        return !getEquipment(part).isEmpty();
    }

    public Collection<ItemStack> getVisibleEquipment() {
        return returnCollection(getEquipment(),
                (part) -> isEquipmentVisible(part) ?
                        getEquipment(part) : null);
    }

    public Collection<EquipmentConfig> getItemConfigs() {
        return returnCollection(getVisibleEquipment(), (equipment) -> ((ChassisEquipment) equipment.getItem()).getConfig());
    }

    public ItemStack getEquipment(ChassisPart chassisPart) {
        var id = getPartId(chassisPart);
        if(id == null) {
            id = 0;
        }

        return inventory.getItem(id);
    }

    public void setEquipment(ChassisPart chassisPart, ItemStack itemStack) {
        inventory.setItem(getPartId(chassisPart), itemStack);
    }

    public int getArmorDurability(ChassisPart chassisPart) {
        var itemStack = getEquipment(chassisPart);
        if (itemStack.isEmpty()) return 0;
        return itemStack.getMaxDamage() - itemStack.getDamageValue();
    }

    public void setArmorData(ListTag nbtTags) {
        if (isClientSide)
            deserializeInventory(inventory, nbtTags);
    }

    public void setInventory(ListTag tags) {
        deserializeInventory(inventory, tags);
    }

    protected void initInventory() {
        SimpleContainer inventoryBuff = this.inventory;
        loadSlots();
        this.inventory = new SimpleContainer(inventorySize);
        if (inventoryBuff != null) {
            inventoryBuff.removeListener(this);
            int i = Math.min(inventoryBuff.getContainerSize(), this.inventory.getContainerSize());
            for (int j = 0; j < i; ++j) {
                var itemStack = inventoryBuff.getItem(j);
                if (!itemStack.isEmpty())
                    this.inventory.setItem(j, itemStack.copy());
            }
        }
        this.inventory.addListener(this);
        serializedInventory = serializeInventory(inventory);
        syncDataWithClient();
    }

    protected void syncDataWithClient() {
        if (isServerSide) {
            PacketHandler.getPlayChannel().sendToTrackingEntity(() -> this, new S2CInventoryPacket(this.getId(), serializedInventory));
        }
    }

    protected void saveInventory(CompoundTag compound) {
        if (inventory == null) return;
        compound.put(ITEMS_TAG, serializeInventory(inventory));
    }

    protected void loadInventory(@NotNull CompoundTag compound) {
        ListTag nbtTags = compound.getList(ITEMS_TAG, 10);
        initInventory();
        deserializeInventory(inventory, nbtTags);
    }

    @Nullable
    protected MenuProvider getMenuProvider(){
        return new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new DynamicChassisMenu(containerId, inventory, playerInventory, Chassis.this);
            }

            @Override
            public Component getDisplayName() {
                return Chassis.this.getDisplayName();
            }
        };
    };

    protected float getMinSpeed() {
        return 0.05f;
    }

    private void updateParams() {
        updateTotalArmor();
        updateSpeed();
        if (isClientSide)
            updateBones();
    }

    @OnlyIn(Dist.CLIENT)
    protected void updateBones() {
        attachmentForBone.clear();
        bonesToHide.clear();
        textureForBone.clear();

        for (var stack : getVisibleEquipment()) {
            var item = getAsChassisEquipment(stack);
            var config = item.getConfig();
            if(config == null) continue;
            addAll(bonesToHide, config.hide);

            for (var attachment : config.attachments) {
                if (stream(config.mods).toList().contains(attachment.armor)
                        && !StackUtils.hasAttachment(stack, attachment.armor))
                    continue;

                var bone = GeoUtils.getBone(config.getModel(), attachment.armor);
                if (bone == null) continue;

                var variant = StackUtils.getVariant(stack);

                textureForBone.put(attachment.armor, config.getTexture(variant));

                if (!attachmentForBone.containsKey(attachment.frame))
                    attachmentForBone.put(attachment.frame, arrayListOf(bone));
                else attachmentForBone.get(attachment.frame).add(bone);
            }
        }
    }

    protected void updateSpeed() {
        setSpeed(getSpeedAttribute());
    }

    public void updateTotalArmor() {
        this.totalDefense = 0;
        this.totalToughness = 0;

        for (var part : partIdMap.keySet()) {
            if (getEquipment(part).getItem() instanceof ChassisArmor armorItem) {
                this.totalDefense += armorItem.getMaterial().getDefenseForSlot(part);
                this.totalToughness += armorItem.getMaterial().getToughness();
            }
        }
    }

    public float getSpeedAttribute() {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }
}
