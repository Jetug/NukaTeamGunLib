package com.nukateam.ntgl.client.util.helpers;

import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.data.properties.SightAnimation;
import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

//TODO eventually convert to deserialized objects

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("ConstantConditions")
public final class PropertyHelper {
    public static final Vec3 GUN_DEFAULT_ORIGIN = new Vec3(8.0, 0.0, 8.0);

    public static Vec3 getIronSightCamera(ItemStack stack, WeaponConfig modifiedWeaponConfig) {
        var data = AimingHandler.get().getWeaponData();
        if(data.weapon.getItem() instanceof IWeapon) {
            var zoom = WeaponModifierHelper.getSightOffset(data);
            var cameraX = zoom.x();
            var cameraY = zoom.y();
            var cameraZ = zoom.z();

            var attachment = WeaponStateHelper.getAttachmentItem(AttachmentType.SCOPE, stack);
            if (!attachment.isEmpty()) {
                var scope = (ScopeItem) attachment.getItem();
                var attachmentConfig = modifiedWeaponConfig.findAttachment(scope);
                cameraX += attachmentConfig.getOffset().x;
                cameraY += attachmentConfig.getOffset().y;
                cameraZ += attachmentConfig.getOffset().z;
            }

            return new Vec3(cameraX, cameraY, cameraZ);
        }
        return Vec3.ZERO;
    }

    public static SightAnimation getSightAnimations(ItemStack weapon) {
        return SightAnimation.DEFAULT;
    }
}
