package com.nukateam.ntgl.client.render.pose;

import com.mojang.math.Axis;

/**
 * Author: MrCrayfish
 */
public class AimPose {
    private Instance idle = new Instance();
    private Instance aiming = new Instance();

    public Instance getIdle() {
        return this.idle;
    }

    public Instance getAiming() {
        return this.aiming;
    }

    public static class Instance {
        private LimbPose leftArm = new LimbPose();
        private LimbPose rightArm = new LimbPose();
        private float renderYawOffset = 5F;
        private Axis itemTranslate = new Axis();
        private Axis itemRotation = new Axis();

        public LimbPose getLeftArm() {
            return this.leftArm;
        }

        public Instance setLeftArm(LimbPose leftArm) {
            this.leftArm = leftArm;
            return this;
        }

        public LimbPose getRightArm() {
            return this.rightArm;
        }

        public Instance setRightArm(LimbPose rightArm) {
            this.rightArm = rightArm;
            return this;
        }

        public float getRenderYawOffset() {
            return this.renderYawOffset;
        }

        public Instance setRenderYawOffset(float renderYawOffset) {
            this.renderYawOffset = renderYawOffset;
            return this;
        }

        public Axis getItemTranslate() {
            return this.itemTranslate;
        }

        public Instance setItemTranslate(Axis itemTranslate) {
            this.itemTranslate = itemTranslate;
            return this;
        }

        public Axis getItemRotation() {
            return this.itemRotation;
        }

        public Instance setItemRotation(Axis itemRotation) {
            this.itemRotation = itemRotation;
            return this;
        }
    }
}
