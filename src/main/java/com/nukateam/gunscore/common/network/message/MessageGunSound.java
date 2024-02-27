package com.nukateam.gunscore.common.network.message;

import com.nukateam.gunscore.client.ClientPlayHandler;
import com.mrcrayfish.framework.api.network.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageGunSound extends PlayMessage<MessageGunSound> {
    private ResourceLocation id;
    private SoundSource category;
    private float x;
    private float y;
    private float z;
    private float volume;
    private float pitch;
    private int shooterId;
    private boolean muzzle;
    private boolean reload;

    public MessageGunSound() {}

    public MessageGunSound(ResourceLocation id, SoundSource category, LivingEntity shooter,
                           float volume, float pitch, boolean muzzle, boolean reload) {
        this.id = id;
        this.category = category;
        this.x = (float)shooter.position().x;
        this.y = (float)shooter.position().y;
        this.z = (float)shooter.position().z;
        this.volume = volume;
        this.pitch = pitch;
        this.shooterId = shooter.getId();
        this.muzzle = muzzle;
        this.reload = reload;
    }

    public MessageGunSound(ResourceLocation id, SoundSource category, Vec3 position,
                           float volume, float pitch, int shooterId, boolean muzzle, boolean reload) {
        this.id = id;
        this.category = category;
        this.x = (float)position.x;
        this.y = (float)position.y;
        this.z = (float)position.z;
        this.volume = volume;
        this.pitch = pitch;
        this.shooterId = shooterId;
        this.muzzle = muzzle;
        this.reload = reload;
    }

    public MessageGunSound(ResourceLocation id, SoundSource category, float x, float y, float z,
                           float volume, float pitch, int shooterId, boolean muzzle, boolean reload) {
        this.id = id;
        this.category = category;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
        this.shooterId = shooterId;
        this.muzzle = muzzle;
        this.reload = reload;
    }

    @Override
    public void encode(MessageGunSound message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.id);
        buffer.writeEnum(message.category);
        buffer.writeFloat(message.x);
        buffer.writeFloat(message.y);
        buffer.writeFloat(message.z);
        buffer.writeFloat(message.volume);
        buffer.writeFloat(message.pitch);
        buffer.writeInt(message.shooterId);
        buffer.writeBoolean(message.muzzle);
        buffer.writeBoolean(message.reload);
    }

    @Override
    public MessageGunSound decode(FriendlyByteBuf buffer) {
        var id = buffer.readResourceLocation();
        var category = buffer.readEnum(SoundSource.class);
        float x = buffer.readFloat();
        float y = buffer.readFloat();
        float z = buffer.readFloat();
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        int shooterId = buffer.readInt();
        boolean muzzle = buffer.readBoolean();
        boolean reload = buffer.readBoolean();
        return new MessageGunSound(id, category, x, y, z, volume, pitch, shooterId, muzzle, reload);
    }

    @Override
    public void handle(MessageGunSound message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> ClientPlayHandler.handleMessageGunSound(message));
        supplier.get().setPacketHandled(true);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public SoundSource getCategory() {
        return this.category;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getShooterId() {
        return this.shooterId;
    }

    public boolean showMuzzleFlash() {
        return this.muzzle;
    }

    public boolean isReload() {
        return this.reload;
    }
}
