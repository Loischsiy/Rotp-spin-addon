package com.loischsiy.rotpspin.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.loischsiy.rotpspin.entity.BallBreakerEntity;

/**
 * Ball Breaker model: geometry and all keyframes come from the Gecko files
 * ({@code geo/ball_breaker.geo.json}, {@code animations/ball_breaker.animation.json}),
 * no hard-coded animation here (template branch {@code new-model-anim-import}).
 */
public class BallBreakerModel extends HumanoidStandModel<BallBreakerEntity> {

    public BallBreakerModel() {
        super();
    }
}
