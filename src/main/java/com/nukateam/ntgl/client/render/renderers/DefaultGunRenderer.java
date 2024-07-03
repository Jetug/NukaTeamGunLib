package com.nukateam.ntgl.client.render.renderers;

import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.client.model.GeoGunModel;

public class DefaultGunRenderer extends DynamicGunRenderer<GunAnimator> {
    public DefaultGunRenderer() {
        super(new GeoGunModel<>(), GunAnimator::new);
    }
}
