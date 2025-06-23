package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.ntgl.common.base.ConfigSupplier;
import com.nukateam.ntgl.common.data.config.Ammo;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProxyAmmoItem extends Item implements IAmmo<Ammo>, IConfigConsumer<Ammo> {
    private final IGunModifier[] modifiers;
    private Ammo projectile = new Ammo();

    public ProxyAmmoItem(Properties properties, IGunModifier... modifiers) {
        super(properties);
        this.modifiers = modifiers;
    }

    @Override
    public void setConfig(ConfigSupplier<Ammo> supplier) {
        this.projectile = supplier.getConfig();
    }

    public Ammo getAmmo() {
        return this.projectile;
    }

    public IGunModifier[] getModifiers() {
        return this.modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        var maxDamage = getMaxDamage(stack);

        if(maxDamage > 0){
            int ammoCount = maxDamage - getDamage(stack);
            tooltip.add(Component.translatable("info.ntgl.projectile",
                    ChatFormatting.WHITE.toString() + ammoCount + "/" + maxDamage).withStyle(ChatFormatting.GRAY));
        }
    }
}
