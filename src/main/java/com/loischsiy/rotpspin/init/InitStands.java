package com.loischsiy.rotpspin.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.StandEntityBlock;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.action.stand.StandEntityLightAttack;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.init.power.stand.EntityStandRegistryObject;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.action.BallBreakerSenescence;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.entity.BallBreakerEntity;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class InitStands {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), AddonMain.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), AddonMain.MOD_ID);

    // ======================================== Ball Breaker ========================================
    // The visualization of Spin energy from the Zeppeli family's ultimate throw (SBR ch. 83-84).
    // Punch numbers come from the stats below; touch numbers live in SpinConfig (ball_breaker).

    public static final RegistryObject<StandEntityLightAttack> BALL_BREAKER_PUNCH = ACTIONS.register("ball_breaker_punch",
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder()
                    .standPose(new StandPose("punch_light"))));

    public static final RegistryObject<StandEntityHeavyAttack> BALL_BREAKER_HEAVY_PUNCH = ACTIONS.register("ball_breaker_heavy_punch",
            () -> new StandEntityHeavyAttack(new StandEntityHeavyAttack.Builder()
                    .standPose(new StandPose("punch_heavy"))));

    public static final RegistryObject<StandEntityBlock> BALL_BREAKER_BLOCK = ACTIONS.register("ball_breaker_block",
            () -> new StandEntityBlock());

    public static final RegistryObject<BallBreakerSenescence> BALL_BREAKER_SENESCENCE = ACTIONS.register("ball_breaker_senescence",
            () -> new BallBreakerSenescence(new StandEntityAction.Builder()
                    .standPose(BallBreakerSenescence.SENESCENCE_POSE)
                    .staminaCost(SpinConfig.BALL_BREAKER_TOUCH_STAMINA.get().floatValue())
                    .partsRequired(StandPart.ARMS)));

    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<BallBreakerEntity>> STAND_BALL_BREAKER =
            new EntityStandRegistryObject<>("ball_breaker",
                    STANDS,
                    () -> new EntityStandType.Builder<StandStats>()
                    .color(0x7AC74F)
                    .storyPartName(ModStandsInit.PART_7_NAME)
                    .leftClickHotbar(
                            BALL_BREAKER_PUNCH.get(),
                            BALL_BREAKER_SENESCENCE.get()
                            )
                    .rightClickHotbar(
                            BALL_BREAKER_BLOCK.get(),
                            BALL_BREAKER_HEAVY_PUNCH.get()
                            )
                    .defaultStats(StandStats.class, new StandStats.Builder()
                            .tier(6)
                            .power(16)
                            .speed(16)
                            .range(2, 10)
                            .durability(14)
                            .precision(8)
                            .build())
                    .build(),

                    InitEntities.ENTITIES,
                    () -> new StandEntityType<BallBreakerEntity>(BallBreakerEntity::new, 0.6F, 1.9F))
            .withDefaultStandAttributes();
}
