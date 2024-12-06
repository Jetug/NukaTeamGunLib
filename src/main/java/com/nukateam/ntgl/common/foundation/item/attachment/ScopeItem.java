package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IScope;
import com.nukateam.ntgl.common.data.attachment.impl.Magazine;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic scope attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class ScopeItem extends AttachmentItem<Scope> {
    public ScopeItem(Scope data, Properties properties) {
        super(AttachmentType.SCOPE, data, properties);
    }
}
