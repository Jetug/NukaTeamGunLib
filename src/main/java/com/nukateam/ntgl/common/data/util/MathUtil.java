package com.nukateam.ntgl.common.data.util;

import java.util.Random;

public class MathUtil {
    public static float clamp(float value, float min, float max) {
        if (value > max) {
            return max;
        } else return Math.max(value, min);
    }

    public static double clamp(double value, double min, double max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        } else {
            return value;
        }
    }

    public static int clamp(int value, int min, int max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        } else {
            return value;
        }
    }

    public static int randomInt(Random rand, int min, int max) {
        if (max >= min)
            return min + rand.nextInt((max - min) + 1);
        else
            return max + rand.nextInt((min - max) + 1);
    }

    public static float randomFloat(Random rand, float min, float max) {
        return min + (rand.nextFloat() * (max - min));
    }

    /**
     * Return if all passed integers are withing bounds
     *
     * @param lowerBound
     * @param upperBound
     * @param values
     * @return
     */
    public static boolean allInRange(int lowerBound, int upperBound, Integer... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] < lowerBound || values[i] > upperBound) {
                return false;
            }
        }

        return true;
    }

    public static int min(Integer... values) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }

        return min;
    }

    public static int max(Integer... values) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }

        return max;
    }

    /**
     * get average from multiple ints, rounded to next int.
     *
     * @param values
     * @return
     */
    public static int getAverageHeight(Integer... values) {
        int sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return (int) Math.round(sum / ((double) values.length));
    }


    /**
     * Returns a rotation angle that is inbetween two other rotation angles. par1 and par2 are the angles between which
     * to interpolate, par3 is probably a float between 0.0 and 1.0 that tells us where "between" the two angles we are.
     * Example: par1 = 30, par2 = 50, par3 = 0.5, then return = 40
     */
    public static float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f;

        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F) {
            ;
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
    }

    public static Vec2 polarOffsetXZ(double x, double z, double radius, double angle) {
        x = x + (radius * Math.cos(angle));
        z = z + (radius * Math.sin(angle));

        return new Vec2(x, z);
    }

    public static class Vec2 {
        public double x;
        public double y;

        public Vec2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * returns the squared vector length;
         *
         * @return
         */
        public double lenSquared() {
            return x * x + y * y;
        }

        public double len() {
            return Math.sqrt(this.lenSquared());
        }

        /**
         * normalizes THIS vector
         */
        public void normalize() {
            double len = this.len();
            if (len > 0) {
                double f = 1 / len;
                x = x * f;
                y = y * f;
            }
        }

        /**
         * Gets a new vec2 that is a normalized version of THIS
         *
         * @return
         */
        public Vec2 getNormalized() {
            double len = this.len();
            if (len > 0) {
                double f = 1 / len;
                return new Vec2(this.x * f, this.y * f);
            }
            return new Vec2(0, 0);
        }

        /**
         * Returns Vector A - Vector B
         *
         * @param A
         * @param B
         * @return
         */
        public static Vec2 substract(Vec2 A, Vec2 B) {
            return new Vec2(B.x - A.x, B.y - A.y);
        }

        /**
         * Returns Vector A + Vector B
         *
         * @param A
         * @param B
         * @return
         */
        public static Vec2 add(Vec2 A, Vec2 B) {
            return new Vec2(B.x + A.x, B.y + A.y);
        }
    }
}
