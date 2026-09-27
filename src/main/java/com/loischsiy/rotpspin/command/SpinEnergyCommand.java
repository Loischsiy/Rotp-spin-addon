package com.loischsiy.rotpspin.command;

import java.util.Collection;

import com.github.standobyte.jojo.command.JojoCommandsCommand;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.capability.SpinPower;
import com.loischsiy.rotpspin.capability.SpinPowerCapability;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.SpinPowerType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * {@code /spinenergy set <targets> <amount> [points|ratio]} and {@code /spinenergy get <target>}
 * (pattern: RotP JojoEnergyCommand). Needed because RotP's energy only mirrors
 * {@link SpinPowerCapability}, so {@code /jojoenergy} is overwritten on the next tick.
 */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SpinEnergyCommand {
    public static final String LITERAL = "spinenergy";
    private static final String KEY = "commands." + AddonMain.MOD_ID + "." + LITERAL;

    private static final SimpleCommandExceptionType RATIO_INVALID = new SimpleCommandExceptionType(
            new TranslationTextComponent(KEY + ".set.ratio.invalid"));
    private static final DynamicCommandExceptionType SINGLE_NO_SPIN = new DynamicCommandExceptionType(
            name -> new TranslationTextComponent(KEY + ".failed.single.no_spin", name));
    private static final DynamicCommandExceptionType MULTIPLE_NO_SPIN = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent(KEY + ".failed.multiple.no_spin", count));

    private enum NumType { POINTS, RATIO }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal(LITERAL).requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                .executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"),
                                        FloatArgumentType.getFloat(ctx, "amount"), NumType.POINTS))
                                .then(Commands.literal("points")
                                        .executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"),
                                                FloatArgumentType.getFloat(ctx, "amount"), NumType.POINTS)))
                                .then(Commands.literal("ratio")
                                        .executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"),
                                                FloatArgumentType.getFloat(ctx, "amount"), NumType.RATIO))))))
                .then(Commands.literal("get").then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> get(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"))))));
        JojoCommandsCommand.addCommand(LITERAL);
    }

    private static int set(CommandSource source, Collection<ServerPlayerEntity> targets, float value, NumType numType)
            throws CommandSyntaxException {
        if (numType == NumType.RATIO && value > 1) {
            throw RATIO_INVALID.create();
        }
        float max = SpinConfig.ENERGY_MAX.get().floatValue();
        float amount = numType == NumType.RATIO ? value * max : value;
        int count = 0;
        for (ServerPlayerEntity player : targets) {
            // Only Spin users: energy of a player without the power would neither regenerate nor be usable.
            if (SpinPowerType.hasSpin(player) && SpinPowerCapability.get(player).map(spin -> {
                spin.setEnergy(amount, max); // synced to the client by SpinPowerCapability#onPlayerTick
                return true;
            }).orElse(false)) {
                count++;
            }
        }
        if (count == 0) {
            if (targets.size() == 1) {
                throw SINGLE_NO_SPIN.create(targets.iterator().next().getName());
            }
            throw MULTIPLE_NO_SPIN.create(targets.size());
        }
        String type = numType.name().toLowerCase();
        if (targets.size() == 1) {
            source.sendSuccess(new TranslationTextComponent(KEY + ".set.success.single." + type,
                    value, targets.iterator().next().getDisplayName()), true);
        }
        else {
            source.sendSuccess(new TranslationTextComponent(KEY + ".set.success.multiple." + type, value, count), true);
        }
        return count;
    }

    private static int get(CommandSource source, ServerPlayerEntity target) throws CommandSyntaxException {
        if (!SpinPowerType.hasSpin(target)) {
            throw SINGLE_NO_SPIN.create(target.getName());
        }
        float energy = SpinPowerCapability.get(target).map(SpinPower::getEnergy).orElse(0F);
        float max = SpinConfig.ENERGY_MAX.get().floatValue();
        source.sendSuccess(new TranslationTextComponent(KEY + ".get.success",
                target.getDisplayName(), String.format("%.1f", energy), String.format("%.1f", max),
                String.format("%.4f", max > 0 ? energy / max : 0)), false);
        return (int) energy;
    }
}
