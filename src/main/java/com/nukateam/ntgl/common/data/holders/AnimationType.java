package com.nukateam.ntgl.common.data.holders;

import com.google.gson.JsonParseException;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.util.PlayerAnimations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class AnimationType extends ResourceHolder {
    public static final AnimationType FIRE = new AnimationType("fire", PlayerAnimations::playFireAnimation);
    public static final AnimationType RELOAD = new AnimationType("reload", PlayerAnimations::playReloadAnimation);
    public static final AnimationType MELEE = new AnimationType("melee", PlayerAnimations::playMeleeAnimation);

    private static final Map<ResourceLocation, AnimationType> typeMap = new HashMap<>();
    private final BiConsumer<AbstractClientPlayer, InteractionHand> animation;

    public static void register(){
        registerType(FIRE);
        registerType(RELOAD);
        registerType(MELEE);
    }

    public AnimationType(ResourceLocation id, BiConsumer<AbstractClientPlayer, InteractionHand> animation) {
        super(id);
        this.animation = animation;
    }

    private AnimationType(String name, BiConsumer<AbstractClientPlayer, InteractionHand> animation) {
        this(ResourceLocation.tryBuild(Ntgl.MOD_ID, name), animation);
    }

    public void playAnimation(AbstractClientPlayer player, InteractionHand hand){
        if (Ntgl.playerAnimatorLoaded) {
            animation.accept(player, hand);
        }
    }

    public static void registerType(AnimationType mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AnimationType getType(ResourceLocation id) {
//        return typeMap.getOrDefault(id, FIRE);

        var type = typeMap.get(id);
        if(type == null){
            throw new JsonParseException("Animation type \"" + id.toString() + "\" doesn't exists");
        }
        return type;
    }

    public static AnimationType getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
