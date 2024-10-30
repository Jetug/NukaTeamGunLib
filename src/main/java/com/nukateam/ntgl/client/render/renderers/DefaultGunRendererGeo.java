package com.nukateam.ntgl.client.render.renderers;

import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.client.model.GeoGunModel;

public class DefaultGunRendererGeo extends DynamicGunRenderer<GunAnimator> {
    public DefaultGunRendererGeo() {
        super(new GeoGunModel<>(), GunAnimator::new);
    }
}
