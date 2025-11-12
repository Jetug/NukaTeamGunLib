package com.nukateam.chassis_core.modules.example.common.entities;

import com.nukateam.chassis_core.client.animators.HandAnimator;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import org.jetbrains.annotations.NotNull;

public class ExampleChassisHand extends HandAnimator {

    @Override
    protected AnimationController.@NotNull AnimationStateHandler<HandAnimator> predicate() {
        return event -> {
//            var controller = event.getController();
//            controller.setAnimationSpeed(1);
//            RawAnimation animation = null;
//            var chassis = getPlayerChassis();
//            if(chassis == null) return PlayState.STOP;
//
//            var player = getLocalPlayer();
//
//            if(player.swinging){
//                controller.setAnimationSpeed(2);
//                animation = begin().then(HIT, LOOP);
//            }
//            else if(chassis.isWalking()){
//                animation = begin().then(WALK, LOOP);
//            }
//            else {
//                animation = begin().then(IDLE, LOOP);
//            }
//            return event.setAndContinue(animation);

            return PlayState.STOP;
        };
    }
}
