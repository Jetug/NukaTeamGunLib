package com.nukateam.ntgl.common.data.attachment.impl;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.WeaponHelper;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * The base attachment object
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class Attachment {
    protected IWeaponModifier[] modifiers;
    private List<Component> perks = null;
    private List<ItemStack> weapons = null;

    public Attachment(IWeaponModifier... modifiers) {
        this.modifiers = modifiers;
    }

    public IWeaponModifier[] getModifiers() {
        return this.modifiers;
    }

    void setPerks(List<Component> perks) {
        if (this.perks == null) {
            this.perks = perks;
        }
    }

    void setWeapons(List<ItemStack> perks) {
        if (this.weapons == null) {
            this.weapons = perks;
        }
    }

    public List<ItemStack> getWeapons(Item attachment) {
        if (this.weapons != null && !this.weapons.isEmpty()) {
            return this.weapons;
        }

        weapons = new ArrayList<>();

        var weaponItems = WeaponHelper.getWeaponItems();
        var id = ForgeRegistries.ITEMS.getKey(attachment);

        for (var item : weaponItems) {
            var weapon = (IWeapon)item;
            if(hasAttachment(weapon, id)){
                weapons.add(new ItemStack(item));
            }
        }

        return this.weapons;
    }

    private static boolean hasAttachment(IWeapon weapon, ResourceLocation id) {
        var list = weapon.getConfig().getModules().getAttachments().values();

        for (var configs : list){
            for (var config: configs){
                if(config.getItemId() != null && config.getItemId().equals(id)){
                    return true;
                }
            }
        }
        return false;
    }

    public List<Component> getPerks(ItemStack stack) {
        if (this.perks != null && !this.perks.isEmpty()) {
            return this.perks;
        }

        var data = new WeaponData(new ItemStack(ModGuns.CLASSIC10MM.get()), null);
        data.attachment = stack;
        var perks = new ArrayList<Component>();

        getNumericPerk(perks, "perk.ntgl.max_ammo", true,
                (modifier, val) -> (float)modifier.modifyMaxAmmo((int)(float)val, data));

        getNumericPerk(perks, "perk.ntgl.projectile_amount",
                (modifier, val) -> (float)modifier.modifyProjectileAmount((int)(float)val, data));

        fireSoundVolume(data, perks);
        silenced(data, perks);
        soundRadius(data, perks);
        damage(data, perks);
        meleeDamage(data, perks);
        meleeDistance(data, perks);
        spread(data, perks);
        life(data, perks);
        recoil(data, perks);
        adsSpeed(data, perks);
        rate(data, perks);
        fireModes(data, perks);
        setPerks(perks);
        return this.perks;
    }

    private void fireSoundVolume(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.fire_volume",
                (modifier, val) -> modifier.modifyFireSoundVolume(val, data));
    }

    private void silenced(WeaponData data, ArrayList<Component> positivePerks) {
        for (var modifier : modifiers) {
            if (modifier.silencedFire(false, data)) {
                addPerk(positivePerks, true, "perk.ntgl.silenced");
                break;
            }
        }
    }

    private void soundRadius(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.sound_radius",
                (modifier, val) -> (float) modifier.modifyFireSoundRadius(val, data));
    }

    private void fireModes(WeaponData data, ArrayList<Component> positivePerks) {
        var inputFireModes = new HashSet<FireMode>();
        Set<FireMode> outputFireModes = new HashSet<>();

        for (var modifier : modifiers) {
            outputFireModes = modifier.modifyFireModes(inputFireModes, data);
        }

        if (!outputFireModes.equals(inputFireModes)) {
            var modes = Component.empty();

            for (var fireMode : outputFireModes) {
                modes.append(fireMode.getDisplayName()).append("; ");
            }

            addPerk(positivePerks, "perk.ntgl.fire_modes", modes);
        }
    }

    private void rate(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.rate", (modifier, val) ->
                (float)modifier.modifyFireRate((int)(float)val, data));
    }

    private void adsSpeed(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.ads_speed", true,
                (modifier, val) -> (float)modifier.modifyAimDownSightSpeed(val, data));
    }

    private void recoil(WeaponData data, ArrayList<Component> positivePerks) {
        float inputRecoil = 1.0f;
        float outputRecoil = inputRecoil;
        for (var modifier : modifiers) {
            outputRecoil *= modifier.recoilModifier(data);
        }
        if (outputRecoil != inputRecoil) {
            var percent = getPercent(outputRecoil / inputRecoil);
            addPerk(positivePerks, outputRecoil < inputRecoil, "perk.ntgl.recoil", percent);
        }
    }

    private void life(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.projectile_life", true,
                (modifier, val) -> (float)modifier.modifyProjectileLife((int)(float)val, data));
    }

    private void spread(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.projectile_spread",
                (modifier, val) -> modifier.modifyProjectileSpread(val, data));
    }

    private void damage(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.modified_damage", true,
                (modifier, val) -> modifier.modifyDamage(val, data));
    }

    private void meleeDamage(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.melee_damage", true,
                (modifier, val) -> modifier.modifyMeleeDamage(val, data));
    }

    private void meleeDistance(WeaponData data, ArrayList<Component> positivePerks) {
        getNumericPerk(positivePerks, "perk.ntgl.melee_distance",
                (modifier, val) -> modifier.modifyMeleeDistance(val, data));
    }

    private void getNumericPerk(ArrayList<Component> positivePerks, String name, BiFunction<IWeaponModifier, Float, Float> function) {
        getNumericPerk(positivePerks,  name, false, function);
    }

    private void getNumericPerk(ArrayList<Component> positivePerks, String name, boolean invert, BiFunction<IWeaponModifier, Float, Float> function) {
        float input1 = 1.0f;
        float input2 = 2.0f;
        float output1 = input1;
        float output2 = input2;

        for (var modifier : modifiers) {
            output1 = function.apply(modifier, output1);
            output2 = function.apply(modifier, output2);
        }
        if (output1 != input1) {
            var outputRatio1 = output1 / input1;
            var outputRatio2 = output2 / input2;
            var value = "";

            if(outputRatio1 == outputRatio2) {
                value = getPercent(outputRatio1);
            }
            else {
                float num = 0f;
                for (var modifier : modifiers) {
                    num = function.apply(modifier, num);
                }
                value = getValue(num, output1 != output2);
            }

            var positive = invert ? output1 > input1 : output1 < input1;
            addPerk(positivePerks, positive, name, value);
        }
    }

    private static String getPercent(double value){
        var percent = Math.abs((1 - value) * 100f);
        var formated = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(percent);
        var sign = value >= 1 ? "+" : "-";
        return sign + formated + "%";
    }

    private static String getValue(double value, boolean signed){
        var formated = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value);
        var sign = value > 0 && signed ? "+" : "";
        return sign + formated;
    }

    private static void addPerk(List<Component> components, boolean positive, String id, Object... params) {
        var icon = positive ? "perk.ntgl.entry.positive" : "perk.ntgl.entry.negative";
        var style = positive ? ChatFormatting.DARK_AQUA : ChatFormatting.GOLD;

        if(params.length == 1 && params[0] instanceof String value){
            components.add(Component.translatable(icon, Component.translatable(id, ChatFormatting.WHITE + value).withStyle(ChatFormatting.GRAY))
                    .withStyle(style));
        }
        else {
            components.add(Component.translatable(icon, Component.translatable(id, params).withStyle(ChatFormatting.GRAY))
                    .withStyle(style));
        }
    }

    private static void addPerk(List<Component> components, String id, Component param) {
        components.add(Component.literal("   ")
                .append(Component.translatable(id).withStyle(ChatFormatting.GRAY))
                .append(param).withStyle(ChatFormatting.WHITE));
    }
}
