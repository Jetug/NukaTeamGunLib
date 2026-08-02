package com.nukateam.ntgl.common.compat.sable;

import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public final class SableSupport {

    private SableSupport() {}

    /**
     * Corrected global-space distance-squared between two points, either of which may sit inside
     * a sub-level's plot.
     */
    public static double distanceSquared(Level level, Vec3 a, Vec3 b) {
        return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, a, b);
    }

    /** Projects a position out of a sub-level plot into global space; a safe no-op otherwise. */
    public static Vec3 toGlobal(Level level, Vec3 pos) {
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, pos);
    }

    /** Same idea as {@link #toGlobal(Level, Vec3)}, for a block position. */
    public static BlockPos toGlobalBlockPos(Level level, BlockPos pos) {
        Vec3 global = toGlobal(level, Vec3.atCenterOf(pos));
        return BlockPos.containing(global);
    }

    /**
     * Raytraces the given GLOBAL-space segment against every sub-level whose current global
     * footprint overlaps it, returning the closest hit (or {@code null} if none).
     * <p>
     * The returned {@link BlockHitResult}'s {@code getBlockPos()} is intentionally LOCAL
     * (the real storage location of the block, needed for {@code getBlockState}, block breaking,
     * redstone, etc.) while its {@code getLocation()} has already been converted to GLOBAL space
     * (needed for entity-search truncation, network packets, particle/explosion positions).
     * This mixed representation matches how {@code ProjectileEntity} consumes the result: block
     * lookups use {@code getBlockPos()}, everything user-visible uses {@code getLocation()}
     * (optionally passed through {@link #toGlobal} again, which is a safe no-op on an
     * already-global point).
     */
    public static BlockHitResult findSubLevelBlockHit(Level level, Vec3 start, Vec3 end,
                                                      Predicate<BlockState> blockFilter,
                                                      Entity self) {
        AABB sweptAabb = new AABB(start, end).inflate(1.0);
        BoundingBox3d sweptBox = new BoundingBox3d(sweptAabb);

        BlockHitResult best = null;
        double bestDistSqr = Double.MAX_VALUE;

        for (SubLevelAccess subLevel : SableCompanion.INSTANCE.getAllIntersecting(level, sweptBox)) {
            Pose3dc pose = subLevel.logicalPose();

            Vec3 localStart = pose.transformPositionInverse(start);
            Vec3 localEnd = pose.transformPositionInverse(end);

            ClipContext clipContext = new ClipContext(localStart, localEnd,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, self);
            BlockHitResult localResult = ProjectileEntity.rayTraceBlocks(level, clipContext, blockFilter);

            if (localResult.getType() == HitResult.Type.MISS) continue;

            Vec3 globalHitPos = pose.transformPosition(localResult.getLocation());
            double distSqr = start.distanceToSqr(globalHitPos);

            if (distSqr < bestDistSqr) {
                bestDistSqr = distSqr;
                best = new BlockHitResult(globalHitPos, localResult.getDirection(),
                        localResult.getBlockPos(), localResult.isInside());
            }
        }

        return best;
    }
}
