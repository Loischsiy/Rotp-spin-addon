package com.loischsiy.rotpspin.entity;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.world.World;

/**
 * Ball Breaker: the visualization of Spin energy from the Zeppeli family's ultimate throw.
 * Geometry and animations come from the Gecko files (docs/art/ball_breaker.md).
 */
public class BallBreakerEntity extends StandEntity {

    public BallBreakerEntity(StandEntityType<BallBreakerEntity> type, World world) {
        super(type, world);
    }
}
