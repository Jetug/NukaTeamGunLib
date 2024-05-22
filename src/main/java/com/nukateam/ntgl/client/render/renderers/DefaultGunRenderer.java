package com.nukateam.ntgl.client.render.renderers;

import com.nukateam.ntgl.client.animators.GunItemAnimator;
import com.nukateam.ntgl.client.model.GeoGunModel;

public class DefaultGunRenderer extends DynamicGunRenderer<GunItemAnimator> {
    public DefaultGunRenderer() {
        super(new GeoGunModel<>(), GunItemAnimator::new);
    }
}
