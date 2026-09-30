package com.loischsiy.rotpspin.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.action.SpinBallSteer;
import com.loischsiy.rotpspin.action.SpinBallThrow;
import com.loischsiy.rotpspin.action.SpinBlockThrow;
import com.loischsiy.rotpspin.action.SpinBodyBrace;
import com.loischsiy.rotpspin.action.SpinGoldenFrame;
import com.loischsiy.rotpspin.action.SpinHealing;
import com.loischsiy.rotpspin.action.SpinItemThrow;
import com.loischsiy.rotpspin.action.SpinMuscleHijack;
import com.loischsiy.rotpspin.power.SpinPowerType;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class InitPowers {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<NonStandPowerType<?>> NON_STAND_POWERS = DeferredRegister.create(
            (Class<NonStandPowerType<?>>) ((Class<?>) NonStandPowerType.class), AddonMain.MOD_ID);

    // ---- Spin actions (registered in the shared Action register, pattern: RotP ModZombieActions) ----
    // Numbers (costs, cooldowns, ranges) live in SpinConfig and are read by the action classes at runtime.

    public static final RegistryObject<SpinBallThrow> SPIN_BALL_THROW = InitStands.ACTIONS.register("spin_ball_throw",
            () -> new SpinBallThrow(new NonStandAction.Builder()));

    public static final RegistryObject<SpinMuscleHijack> SPIN_MUSCLE_HIJACK = InitStands.ACTIONS.register("spin_muscle_hijack",
            () -> new SpinMuscleHijack(new NonStandAction.Builder().swingHand()));

    public static final RegistryObject<SpinItemThrow> SPIN_ITEM_THROW = InitStands.ACTIONS.register("spin_item_throw",
            () -> new SpinItemThrow(new NonStandAction.Builder()));

    public static final RegistryObject<SpinBlockThrow> SPIN_BLOCK_THROW = InitStands.ACTIONS.register("spin_block_throw",
            () -> new SpinBlockThrow(new NonStandAction.Builder()));

    public static final RegistryObject<SpinBallSteer> SPIN_BALL_STEER = InitStands.ACTIONS.register("spin_ball_steer",
            () -> new SpinBallSteer(new NonStandAction.Builder().holdType()));

    public static final RegistryObject<SpinHealing> SPIN_HEALING = InitStands.ACTIONS.register("spin_healing",
            () -> new SpinHealing(new NonStandAction.Builder().holdType()));

    // Hold duration is capped here; the framing moment itself is read from SpinConfig at runtime.
    public static final RegistryObject<SpinGoldenFrame> SPIN_GOLDEN_FRAME = InitStands.ACTIONS.register("spin_golden_frame",
            () -> new SpinGoldenFrame(new NonStandAction.Builder().holdType(1200)));

    // Lesson 1: Spin on one's own body. Windup and costs are read from SpinConfig at runtime.
    public static final RegistryObject<SpinBodyBrace> SPIN_BODY_BRACE = InitStands.ACTIONS.register("spin_body_brace",
            () -> new SpinBodyBrace(new NonStandAction.Builder().holdType(1200)));

    // Given with /jojopower give <player> rotp_spin:spin (learning from the Zeppeli family comes later).
    @SuppressWarnings("unchecked")
    public static final RegistryObject<SpinPowerType> SPIN = NON_STAND_POWERS.register("spin",
            () -> new SpinPowerType(
                    (Action<INonStandPower>[]) new Action<?>[] {
                            SPIN_BALL_THROW.get(),
                            SPIN_MUSCLE_HIJACK.get(),
                            SPIN_ITEM_THROW.get(),
                            SPIN_BLOCK_THROW.get() },
                    (Action<INonStandPower>[]) new Action<?>[] {
                            SPIN_BALL_STEER.get(),
                            SPIN_HEALING.get(),
                            SPIN_GOLDEN_FRAME.get(),
                            SPIN_BODY_BRACE.get() },
                    SPIN_BALL_THROW.get()
            ).withColor(SpinPowerType.COLOR));
}
