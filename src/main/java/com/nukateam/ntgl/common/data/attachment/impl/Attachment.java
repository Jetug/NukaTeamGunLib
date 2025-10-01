package com.nukateam.ntgl.common.data.attachment.impl;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponHelper;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
 * <p>
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class Attachment {
    protected IGunModifier[] modifiers;
    private List<Component> perks = null;
    private List<ItemStack> weapons = null;

    public Attachment(IGunModifier... modifiers) {
        this.modifiers = modifiers;
    }

    public IGunModifier[] getModifiers() {
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

    public List<Component> getPerks() {
        if (this.perks != null && !this.perks.isEmpty()) {
            return this.perks;
        }

        var player = Minecraft.getInstance().player;
        if (player == null) return List.of();

        var data = new GunData(new ItemStack(ModGuns.CLASSIC10MM.get()), player);
        var perks = new ArrayList<Component>();

        fireSoundVolume(data, perks);
        silenced(data, perks);
        soundRadius(data, perks);
        damage(data, perks);
        meleeDamage(data, perks);
        meleeDistance(data, perks);
        speed(data, perks);
        spread(data, perks);
        life(data, perks);
        recoil(data, perks);
        adsSpeed(data, perks);
        rate(data, perks);
        fireModes(data, perks);
        setPerks(perks);
        return this.perks;
    }

    private void fireSoundVolume(GunData data, ArrayList<Component> positivePerks) {
        float inputSound = 1f;
        float outputSound = inputSound;

        for (var modifier : modifiers) {
            outputSound = modifier.modifyFireSoundVolume(outputSound, data);
        }
        if (outputSound != inputSound) {
            var percent = getPercent(outputSound / inputSound);
            addPerk(positivePerks, outputSound < inputSound, "perk.ntgl.fire_volume", percent);
        }
    }

    private void silenced(GunData data, ArrayList<Component> positivePerks) {
        for (var modifier : modifiers) {
            if (modifier.silencedFire(data)) {
                addPerk(positivePerks, true, "perk.ntgl.silenced.positive");
                break;
            }
        }
    }

    private void soundRadius(GunData data, ArrayList<Component> positivePerks) {
        double inputRadius = 1.0;
        double outputRadius = inputRadius;
        for (var modifier : modifiers) {
            outputRadius = modifier.modifyFireSoundRadius(outputRadius, data);
        }
        if (outputRadius != inputRadius) {
            var percent = getPercent(outputRadius / inputRadius);
            addPerk(positivePerks, outputRadius < inputRadius, "perk.ntgl.sound_radius.positive", percent);
        }
    }

    private void fireModes(GunData data, ArrayList<Component> positivePerks) {
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

    private void rate(GunData data, ArrayList<Component> positivePerks) {

        rate2((modifier, val) -> modifier.modifyFireRate(output, data));

        for (var modifier : modifiers) {
            output = modifier.modifyFireRate(output, data);
            output2 = modifier.modifyFireRate(output2, data);
        }
    }

    private void rate2(GunData data, ArrayList<Component> positivePerks, BiFunction<IGunModifier, Double, Double> function) {
        int input = 1;
        int output = input;

        int input2 = 2;
        int output2 = input2;

        for (var modifier : modifiers) {
            output = modifier.modifyFireRate(output, data);
            output2 = modifier.modifyFireRate(output2, data);
        }
        if (output != input) {
            var p1 = output / (float) input;
            var p2 = output2 / (float) input2;

            var value = "";
            if(p1 == p2) {
                value = getPercent(p1);
            }
            else {
                int num = 0;
                for (var modifier : modifiers) {
                    num = modifier.modifyFireRate(num, data);
                }
                value = getValue(num);
            }

            addPerk(positivePerks, output < input, "perk.ntgl.rate", value);
        }
    }

    private void adsSpeed(GunData data, ArrayList<Component> positivePerks) {
        double inputAdsSpeed = 1.0f;
        double outputAdsSpeed = inputAdsSpeed;

        for (var modifier : modifiers) {
            outputAdsSpeed = modifier.modifyAimDownSightSpeed(outputAdsSpeed, data);
        }
        if (outputAdsSpeed != inputAdsSpeed) {
            var percent = getPercent(outputAdsSpeed / inputAdsSpeed);
            addPerk(positivePerks, outputAdsSpeed > inputAdsSpeed, "perk.ntgl.ads_speed", percent);
        }
    }

    private void recoil(GunData data, ArrayList<Component> positivePerks) {
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

    private void life(GunData data, ArrayList<Component> positivePerks) {
        int inputLife = 1;
        int outputLife = inputLife;

        for (var modifier : modifiers) {
            outputLife = modifier.modifyProjectileLife(outputLife, data);
        }
        if (outputLife != inputLife) {
            var percent = getPercent((float)outputLife / (float)inputLife);
            addPerk(positivePerks, outputLife > inputLife, "perk.ntgl.projectile_life", percent);
        }
    }

    private void spread(GunData data, ArrayList<Component> positivePerks) {
        var inputSpread = 1.0f;
        var outputSpread = inputSpread;

        for (var modifier : modifiers) {
            outputSpread = modifier.modifyProjectileSpread(outputSpread, data);
        }

        if (outputSpread != inputSpread) {
            var percent = getPercent(outputSpread / inputSpread);
            addPerk(positivePerks, outputSpread < inputSpread, "perk.ntgl.projectile_spread", percent);
        }
    }

    private void speed(GunData data, ArrayList<Component> positivePerks) {
        double inputSpeed = 1.0f;
        double outputSpeed = inputSpeed;
        for (var modifier : modifiers) {
            outputSpeed = modifier.modifyProjectileSpeed(outputSpeed, data);
        }

        if (outputSpeed != inputSpeed) {
            var percent = getPercent(outputSpeed / inputSpeed);
            addPerk(positivePerks, outputSpeed > inputSpeed, "perk.ntgl.projectile_speed", percent);
        }
    }

    private void damage(GunData data, ArrayList<Component> positivePerks) {
        float input = 1.0f;
        float output = input;
        for (var modifier : getModifiers()) {
            output = modifier.modifyDamage(output, data);
        }
        if (output != input) {
            var percent = getPercent(output / input);
            addPerk(positivePerks, output > input, "perk.ntgl.modified_damage", percent);
        }
    }

    private void meleeDamage(GunData data, ArrayList<Component> positivePerks) {
        float input = 1.0f;
        float output = input;
        for (var modifier : getModifiers()) {
            output = modifier.modifyMeleeDamage(output, data);
        }
        if (output != input) {
            var percent = getPercent(output / input);
            addPerk(positivePerks, output > input, "perk.ntgl.melee_damage", percent);
        }
    }

    private void meleeDistance(GunData data, ArrayList<Component> positivePerks) {
        float input = 1.0f;
        float output = input;
        for (var modifier : getModifiers()) {
            output = modifier.modifyMeleeDistance(output, data);
        }
        if (output != input) {
            addPerk(positivePerks, output > input, "perk.ntgl.melee_distance");
        }
    }

    private static String getPercent(double value){
        var percent = Math.abs((1 - value) * 100f);
        var formated = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(percent);
        var sign = value >= 1 ? "+" : "-";
        return sign + formated + "%";
    }

    private static String getValue(double value){
        var formated = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value);
        var sign = value > 0 ? "+" : "";
        return sign + formated;
    }

    private static void addPerk(List<Component> components, boolean positive, String id, Object... params) {
        var icon = positive ? "perk.ntgl.entry.positive" : "perk.ntgl.entry.negative";
        var style = positive ? ChatFormatting.DARK_AQUA : ChatFormatting.GOLD;

        components.add(Component.translatable(icon, Component.translatable(id, params).withStyle(ChatFormatting.WHITE))
                .withStyle(style));
    }

    private static void addPerk(List<Component> components, String id, Component param) {
        components.add(Component.literal("   ")
                .append(Component.translatable(id).withStyle(ChatFormatting.WHITE))
                .append(param).withStyle(ChatFormatting.GRAY));
    }
}
