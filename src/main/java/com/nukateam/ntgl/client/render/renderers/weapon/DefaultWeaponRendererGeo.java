package com.nukateam.ntgl.client.render.renderers.weapon;

import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.client.model.gun.GeoWeaponModel;

public class DefaultWeaponRendererGeo extends DynamicWeaponRenderer {
    public DefaultWeaponRendererGeo() {
        super(new GeoWeaponModel());
    }
}
