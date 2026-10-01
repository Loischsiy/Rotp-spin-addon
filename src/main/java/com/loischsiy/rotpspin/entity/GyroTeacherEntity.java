package com.loischsiy.rotpspin.entity;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.loischsiy.rotpspin.init.InitPowers;
import com.loischsiy.rotpspin.power.SpinData;
import com.loischsiy.rotpspin.power.SpinPowerType;
import com.loischsiy.rotpspin.world.GyroSpawns;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

/**
 * Gyro Zeppeli, the Spin mentor (docs/art/gyro_teacher.md): gives the Spin power instead of
 * the /jojopower command and reports the lesson progress. Does not despawn, does not fight.
 */
public class GyroTeacherEntity extends CreatureEntity {

    public GyroTeacherEntity(EntityType<? extends GyroTeacherEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.addGoal(8, new LookRandomlyGoal(this));
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayerEntity) {
            talk((ServerPlayerEntity) player);
            level.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.VILLAGER_YES, getSoundSource(), 1.0F, 1.0F);
        }
        return ActionResultType.sidedSuccess(level.isClientSide());
    }

    private void talk(ServerPlayerEntity player) {
        if (!SpinPowerType.hasSpin(player)) {
            boolean given = INonStandPower.getNonStandPowerOptional(player)
                    .map(power -> !power.hasPower() && power.givePower(InitPowers.SPIN.get()))
                    .orElse(false);
            player.sendMessage(new TranslationTextComponent(given
                    ? "rotp_spin.message.gyro_spin_given" : "rotp_spin.message.gyro_has_other")
                    .withStyle(given ? TextFormatting.GOLD : TextFormatting.GRAY), Util.NIL_UUID);
            return;
        }
        SpinData.of(player).ifPresent(data -> player.sendMessage(new TranslationTextComponent(
                "rotp_spin.message.gyro_status", data.getLesson(),
                new TranslationTextComponent("rotp_spin.lesson." + data.getLesson()),
                data.getBallHits(), data.getHijacks(), data.getGoldenHits())
                .withStyle(TextFormatting.YELLOW), Util.NIL_UUID));
    }

    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason,
            @Nullable ILivingEntityData data, @Nullable CompoundNBT nbt) {
        ILivingEntityData result = super.finalizeSpawn(world, difficulty, reason, data, nbt);
        GyroSpawns.spawnHorse(world, this, reason);
        return result;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
