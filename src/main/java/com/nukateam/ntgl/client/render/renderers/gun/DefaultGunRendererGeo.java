package com.nukateam.ntgl.client.render.renderers.gun;

import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.client.model.gun.GeoGunModel;

public class DefaultGunRendererGeo extends DynamicGunRenderer<GunAnimator> {
    public DefaultGunRendererGeo() {
        super(new GeoGunModel<>(), GunAnimator::new);
    }
}
