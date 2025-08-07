package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.common.data.config.ProjectileConfig;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.holders.AmmoType;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.data.holders.GrenadeMode;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Set;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

public class ThrowableStateHelper {
    public static GrenadeMode getMode(ItemStack stack) {
        var throwable = (IThrowable)stack.getItem();
        return throwable.getConfig().getGeneral().getMode();
    }
}
