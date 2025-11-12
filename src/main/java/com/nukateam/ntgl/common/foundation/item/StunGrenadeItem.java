package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.ntgl.common.foundation.entity.StunGrenadeEntity;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableGrenadeEntity;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableItemEntity;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;


public class StunGrenadeItem extends WeaponItem {
    public StunGrenadeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected ThrowableGrenadeEntity<StunGrenadeItem> createThrowable(Level world, LivingEntity entity, int timeLeft) {
        return new StunGrenadeEntity<>(world, entity, this, 20 * 2);
    }

    @Override
    protected void onThrown(Level world, ThrowableItemEntity entity) {
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
