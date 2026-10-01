package com.loischsiy.rotpspin.entity;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.BallBreakerAura;

import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.world.World;

/**
 * Ball Breaker: the visualization of Spin energy from the Zeppeli family's ultimate throw.
 * Geometry and animations come from the Gecko files (docs/art/ball_breaker.md).
 * Around it hangs a cloud of energy resembling electrostatic discharges (canon look, ch. 83):
 * client-side particles only, gated like RotP's own Stand particles (ClientUtil.canSeeStands).
 */
public class BallBreakerEntity extends StandEntity {
    /** Pale gold specks of the cloud. */
    private static final RedstoneParticleData AURA_DUST = new RedstoneParticleData(1.0F, 0.9F, 0.5F, 0.6F);
    /** Near-white discharge arcs. */
    private static final RedstoneParticleData ARC_DUST = new RedstoneParticleData(1.0F, 1.0F, 0.85F, 0.4F);
    /** Visual density of an arc: one speck per this many blocks of its length. */
    private static final double ARC_SPECK_SPACING = 0.08;

    public BallBreakerEntity(StandEntityType<BallBreakerEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide() && SpinConfig.BALL_BREAKER_AURA_ENABLED.get()
                && !underInvisibilityEffect() && ClientUtil.canSeeStands()) {
            addAuraParticles();
        }
    }

    private void addAuraParticles() {
        double radius = SpinConfig.BALL_BREAKER_AURA_RADIUS.get();
        double height = BallBreakerAura.verticalRadius(getBbHeight(), radius);
        double cx = getX();
        double cy = getY() + getBbHeight() * 0.5;
        double cz = getZ();
        float alpha = getAlpha(1.0F);

        int sparks = BallBreakerAura.sparkCount(SpinConfig.BALL_BREAKER_AURA_SPARKS.get(), alpha);
        for (int i = 0; i < sparks; i++) {
            double[] p = BallBreakerAura.shellPoint(random.nextDouble(), random.nextDouble(), radius, height);
            level.addParticle(AURA_DUST, cx + p[0], cy + p[1], cz + p[2], 0.0, 0.0, 0.0);
        }

        if (alpha > 0F && random.nextDouble() < SpinConfig.BALL_BREAKER_AURA_ARC_CHANCE.get()) {
            int segments = SpinConfig.BALL_BREAKER_AURA_ARC_SEGMENTS.get();
            double[] from = BallBreakerAura.shellPoint(random.nextDouble(), random.nextDouble(), radius, height);
            double[] to = BallBreakerAura.shellPoint(random.nextDouble(), random.nextDouble(), radius, height);
            double[] randoms = new double[Math.max(0, (segments - 1) * 3)];
            for (int i = 0; i < randoms.length; i++) {
                randoms[i] = random.nextDouble();
            }
            double[][] arc = BallBreakerAura.arc(from, to, segments,
                    SpinConfig.BALL_BREAKER_AURA_ARC_JITTER.get(), randoms);
            for (int i = 1; i < arc.length; i++) {
                double[] a = arc[i - 1];
                double[] b = arc[i];
                double dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
                int specks = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz) / ARC_SPECK_SPACING));
                for (int s = 0; s < specks; s++) {
                    double t = (double) s / specks;
                    level.addParticle(ARC_DUST, cx + a[0] + dx * t, cy + a[1] + dy * t, cz + a[2] + dz * t,
                            0.0, 0.0, 0.0);
                }
            }
        }
    }
}
