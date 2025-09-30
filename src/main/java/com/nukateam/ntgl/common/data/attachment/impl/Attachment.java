package com.nukateam.ntgl.common.data.attachment.impl;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The base attachment object
 * <p>
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class Attachment {
    protected IGunModifier[] modifiers;
    private List<Component> perks = null;

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

    public List<Component> getPerks() {
        if (perks != null && !perks.isEmpty()) {
            return perks;
        }

        var player = Minecraft.getInstance().player;
        if (player == null) return List.of();

        var data = new GunData(new ItemStack(ModGuns.CLASSIC10MM.get()), player);

        var modifiers = getModifiers();
        var positivePerks = new ArrayList<Component>();
//        var negativePerks = new ArrayList<Component>();

        /* Test for fire sound volume */
        float inputSound = 1f;
        float outputSound = inputSound;

        for (var modifier : modifiers) {
            outputSound = modifier.modifyFireSoundVolume(outputSound, data);
        }
        if (outputSound != inputSound) {
            var percent = getPercent(outputSound / inputSound);
            addPerk(positivePerks, outputSound < inputSound, "perk.ntgl.fire_volume", percent);
        }

        /* Test for silenced */
        for (var modifier : modifiers) {
            if (modifier.silencedFire(data)) {
                addPerk(positivePerks, true, "perk.ntgl.silenced.positive");
                break;
            }
        }

        /* Test for sound radius */
        double inputRadius = 1.0;
        double outputRadius = inputRadius;
        for (var modifier : modifiers) {
            outputRadius = modifier.modifyFireSoundRadius(outputRadius, data);
        }
        if (outputRadius != inputRadius) {
            var percent = getPercent(outputRadius / inputRadius);
            addPerk(positivePerks, outputRadius < inputRadius, "perk.ntgl.sound_radius.positive", percent);
        }

//        /* Test for additional damage */
//        var additionalDamage = 0F;
//        for (var modifier : modifiers) {
//            additionalDamage += modifier.additionalDamage(data);
//        }

//        if (additionalDamage != 0) {
//            var value = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage / 2.0);
//            addPerk(positivePerks, additionalDamage > 0.0f, "perk.ntgl.additional_damage.positive", value);
//        }

        /* Test for modified damage */
        float inputDamage = 1.0f;
        float outputDamage = inputDamage;
        for (var modifier : modifiers) {
            outputDamage = modifier.modifyDamage(outputDamage, data);
        }
        if (outputDamage != inputDamage) {
            addPerk(positivePerks, outputDamage > inputDamage, "perk.ntgl.modified_damage");
        }

        /* Test for modified damage */
        double inputSpeed = 1.0f;
        double outputSpeed = inputSpeed;
        for (var modifier : modifiers) {
            outputSpeed = modifier.modifyProjectileSpeed(outputSpeed, data);
        }

        if (outputSpeed != inputSpeed) {
            addPerk(positivePerks, outputSpeed > inputSpeed, "perk.ntgl.projectile_speed");
        }

        /* Test for modified projectile spread */
        var inputSpread = 1.0f;
        var outputSpread = inputSpread;

        for (var modifier : modifiers) {
            outputSpread = modifier.modifyProjectileSpread(outputSpread, data);
        }

        if (outputSpread != inputSpread) {
            var percent = getPercent(outputSpread / inputSpread);
            addPerk(positivePerks, outputSpread < inputSpread, "perk.ntgl.projectile_spread", percent);
        }

        /* Test for modified projectile life */
        int inputLife = 1;
        int outputLife = inputLife;

        for (var modifier : modifiers) {
            outputLife = modifier.modifyProjectileLife(outputLife, data);
        }
        if (outputLife != inputLife) {
            var percent = getPercent((float)outputLife / (float)inputLife);
            addPerk(positivePerks, outputLife > inputLife, "perk.ntgl.projectile_life", percent);
        }

        /* Test for modified recoil */
        float inputRecoil = 1.0f;
        float outputRecoil = inputRecoil;
        for (var modifier : modifiers) {
            outputRecoil *= modifier.recoilModifier(data);
        }
        if (outputRecoil != inputRecoil) {
            var percent = getPercent(outputRecoil / inputRecoil);
            addPerk(positivePerks, outputRecoil < inputRecoil, "perk.ntgl.recoil", percent);
        }

        /* Test for aim down sight speed */
        double inputAdsSpeed = 1.0f;
        double outputAdsSpeed = inputAdsSpeed;

        for (var modifier : modifiers) {
            outputAdsSpeed = modifier.modifyAimDownSightSpeed(outputAdsSpeed, data);
        }
        if (outputAdsSpeed != inputAdsSpeed) {
            var percent = getPercent(outputAdsSpeed / inputAdsSpeed);
            addPerk(positivePerks, outputAdsSpeed > inputAdsSpeed, "perk.ntgl.ads_speed", percent);
        }

        /* Test for fire rate */
        int inputRate = 1;
        int outputRate = inputRate;
        for (var modifier : modifiers) {
            outputRate = modifier.modifyFireRate(outputRate, data);
        }
        if (outputRate != inputRate) {
            var percent = getPercent(outputRate / (float)inputRate);
            addPerk(positivePerks, outputRate < inputRate, "perk.ntgl.rate", percent);
        }

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

//        positivePerks.addAll(negativePerks);
        setPerks(positivePerks);
        return perks;
    }

    private static String getPercent(double value){
        var percent = Math.abs((1 - value) * 100f);
        var formated = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(percent);
        var sign = value >= 1 ? "+" : "-";
        return sign + formated + "%";
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
