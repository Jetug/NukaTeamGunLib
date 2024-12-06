package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.client.util.handler.GunRenderingHandler;

import com.nukateam.ntgl.common.foundation.item.interfaces.IMeta;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

import static com.nukateam.ntgl.common.util.util.ResourceUtils.getResourceName;

/**
 * Author: MrCrayfish
 */
public class AttachmentItemBase extends Item implements IMeta, IResourceProvider {
    private final Lazy<String> name = Lazy.of(() -> getResourceName(ForgeRegistries.ITEMS.getKey(this)));

    public AttachmentItemBase(Properties properties) {
        super(properties);
    }

    /* Dirty hack to apply enchantment effect to attachments if gun is enchanted */
    @Override
    public boolean isFoil(ItemStack stack) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ItemStack weapon = GunRenderingHandler.get().getRenderingWeapon();
            if (weapon != null) {
                return weapon.getItem().isFoil(weapon);
            }
        }
        return super.isFoil(stack);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public String getNamespace() {
        return ForgeRegistries.ITEMS.getKey(this).getNamespace();
    }
}
