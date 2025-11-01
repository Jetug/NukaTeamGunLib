package com.nukateam.ntgl.common.util.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;

import java.util.function.BiFunction;
import java.util.function.Function;

public class RayTraceHelper {
    public static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> onFinish) {
        var startVec = context.getFrom();
        var endVec = context.getTo();

        if (!startVec.equals(endVec)) {
            var startX = Mth.lerp(-0.0000001, endVec.x, startVec.x);
            var startY = Mth.lerp(-0.0000001, endVec.y, startVec.y);
            var startZ = Mth.lerp(-0.0000001, endVec.z, startVec.z);

            var endX = Mth.lerp(-0.0000001, startVec.x, endVec.x);
            var endY = Mth.lerp(-0.0000001, startVec.y, endVec.y);
            var endZ = Mth.lerp(-0.0000001, startVec.z, endVec.z);

            var blockX = Mth.floor(endX);
            var blockY = Mth.floor(endY);
            var blockZ = Mth.floor(endZ);

            var mutablePos = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
            T t = hitFunction.apply(context, mutablePos);

            if (t != null) return t;

            double deltaX = startX - endX;
            double deltaY = startY - endY;
            double deltaZ = startZ - endZ;

            int signX = Mth.sign(deltaX);
            int signY = Mth.sign(deltaY);
            int signZ = Mth.sign(deltaZ);

            double d9 = signX == 0 ? Double.MAX_VALUE : (double) signX / deltaX;
            double d10 = signY == 0 ? Double.MAX_VALUE : (double) signY / deltaY;
            double d11 = signZ == 0 ? Double.MAX_VALUE : (double) signZ / deltaZ;
            double d12 = d9 * (signX > 0 ? 1.0D - Mth.frac(endX) : Mth.frac(endX));
            double d13 = d10 * (signY > 0 ? 1.0D - Mth.frac(endY) : Mth.frac(endY));
            double d14 = d11 * (signZ > 0 ? 1.0D - Mth.frac(endZ) : Mth.frac(endZ));

            while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
                if (d12 < d13) {
                    if (d12 < d14) {
                        blockX += signX;
                        d12 += d9;
                    } else {
                        blockZ += signZ;
                        d14 += d11;
                    }
                } else if (d13 < d14) {
                    blockY += signY;
                    d13 += d10;
                } else {
                    blockZ += signZ;
                    d14 += d11;
                }

                var t1 = hitFunction.apply(context, mutablePos.set(blockX, blockY, blockZ));
                if (t1 != null)
                    return t1;
            }

        }
        return onFinish.apply(context);
    }
}
