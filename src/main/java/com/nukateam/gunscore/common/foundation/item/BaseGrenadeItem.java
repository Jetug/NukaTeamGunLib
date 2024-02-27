package com.nukateam.gunscore.common.foundation.item;

import com.nukateam.gunscore.common.foundation.entity.ThrowableBaseGrenadeEntity;
import com.nukateam.gunscore.common.foundation.entity.ThrowableGrenadeEntity;
import com.nukateam.gunscore.common.foundation.init.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BaseGrenadeItem extends GrenadeItem{
    public BaseGrenadeItem(Properties properties, int maxCookTime) {
        super(properties, maxCookTime);
    }
    @Override
    public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft) {
        return new ThrowableBaseGrenadeEntity(world, entity, timeLeft);
    }
    @Override
    protected void onThrown(Level world, ThrowableGrenadeEntity entity) {
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

}
