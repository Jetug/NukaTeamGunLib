package com.nukateam.example.common.foundation.items.guns;

import com.nukateam.gunscore.common.foundation.item.UnderBarrelItem;
import com.nukateam.gunscore.common.foundation.item.attachment.impl.UnderBarrel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MagazineItem extends UnderBarrelItem {
    public MagazineItem(UnderBarrel underBarrel, Properties properties) {
        super(underBarrel, properties);
    }


    @Override
    public void appendHoverText(ItemStack item, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(item, level, list, flag);
        list.add(new TranslatableComponent("attachment.gunscore.magazine"));

    }
}
