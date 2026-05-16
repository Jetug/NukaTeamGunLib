package com.nukateam.ntgl.client.helpers;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MuzzleMatrixHelper {
    public static Matrix4f lastMuzzleMatrix = null;
    public static Matrix4f lastThirdPersonMuzzleMatrix = null;

    private static final Map<Integer, Matrix4f> firstPersonMatrices = new ConcurrentHashMap<>();
    private static final Map<Integer, Matrix4f> thirdPersonMatrices = new ConcurrentHashMap<>();

    public static void saveMuzzleMatrix(int entityId, Matrix4f mat, boolean isFirstPerson) {
        if (isFirstPerson) {
            firstPersonMatrices.put(entityId, mat);
        } else {
            thirdPersonMatrices.put(entityId, mat);
        }
    }

    public static void clearMatrices() {
        firstPersonMatrices.clear();
        thirdPersonMatrices.clear();
    }

    private static Vec3 viewToWorld(Matrix4f matrix) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera == null || !camera.isInitialized()) return null;

        Vector3f viewPos = new Vector3f(0, 0, 0);
        matrix.transformPosition(viewPos);

        org.joml.Vector3f left = camera.getLeftVector();
        org.joml.Vector3f up = camera.getUpVector();
        org.joml.Vector3f look = camera.getLookVector();

        double wx = -left.x() * viewPos.x() + up.x() * viewPos.y() - look.x() * viewPos.z();
        double wy = -left.y() * viewPos.x() + up.y() * viewPos.y() - look.y() * viewPos.z();
        double wz = -left.z() * viewPos.x() + up.z() * viewPos.y() - look.z() * viewPos.z();

        return camera.getPosition().add(wx, wy, wz);
    }

    public static Vec3 getMuzzleWorldPos(float partialTicks) {
        if (lastMuzzleMatrix == null) return null;
        return viewToWorld(lastMuzzleMatrix);
    }

    public static Vec3 getThirdPersonMuzzleWorldPos(float partialTicks) {
        if (lastThirdPersonMuzzleMatrix == null) return null;
        return viewToWorld(lastThirdPersonMuzzleMatrix);
    }

    public static Vec3 getMuzzleWorldPosForEntity(int entityId, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        boolean isLocalPlayer = mc.player != null && mc.player.getId() == entityId;

        if (isLocalPlayer && mc.options.getCameraType().isFirstPerson()) {
            Matrix4f mat = firstPersonMatrices.get(entityId);
            if (mat == null) mat = lastMuzzleMatrix;
            if (mat == null) return null;
            return viewToWorld(mat);
        }

        Matrix4f mat = thirdPersonMatrices.get(entityId);
        if (isLocalPlayer && mat == null) mat = lastThirdPersonMuzzleMatrix;
        if (mat == null) return null;
        return viewToWorld(mat);
    }
}
