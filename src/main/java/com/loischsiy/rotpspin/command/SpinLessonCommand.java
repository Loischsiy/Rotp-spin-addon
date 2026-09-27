package com.loischsiy.rotpspin.command;

import java.util.Collection;

import com.github.standobyte.jojo.command.JojoCommandsCommand;
import com.loischsiy.rotpspin.AddonMain;
import com.loischsiy.rotpspin.config.SpinConfig;
import com.loischsiy.rotpspin.power.SpinData;
import com.loischsiy.rotpspin.power.SpinLessons;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** {@code /spinlesson set <targets> <1..3>} and {@code /spinlesson get <target>} (same style as /spinenergy). */
@EventBusSubscriber(modid = AddonMain.MOD_ID)
public class SpinLessonCommand {
    public static final String LITERAL = "spinlesson";
    private static final String KEY = "commands." + AddonMain.MOD_ID + "." + LITERAL;

    private static final DynamicCommandExceptionType SINGLE_NO_SPIN = new DynamicCommandExceptionType(
            name -> new TranslationTextComponent("commands.rotp_spin.spinenergy.failed.single.no_spin", name));
    private static final DynamicCommandExceptionType MULTIPLE_NO_SPIN = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.rotp_spin.spinenergy.failed.multiple.no_spin", count));

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal(LITERAL).requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("lesson", IntegerArgumentType.integer(SpinLessons.FIRST, SpinLessons.MAX))
                                .executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"),
                                        IntegerArgumentType.getInteger(ctx, "lesson"))))))
                .then(Commands.literal("get").then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> get(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"))))));
        JojoCommandsCommand.addCommand(LITERAL);
    }

    private static int set(CommandSource source, Collection<ServerPlayerEntity> targets, int lesson) throws CommandSyntaxException {
        int count = 0;
        for (ServerPlayerEntity player : targets) {
            if (SpinData.of(player).map(data -> {
                data.setLesson(lesson, true);
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
        if (targets.size() == 1) {
            source.sendSuccess(new TranslationTextComponent(KEY + ".set.success.single",
                    targets.iterator().next().getDisplayName(), lesson), true);
        }
        else {
            source.sendSuccess(new TranslationTextComponent(KEY + ".set.success.multiple", count, lesson), true);
        }
        return count;
    }

    private static int get(CommandSource source, ServerPlayerEntity target) throws CommandSyntaxException {
        SpinData data = SpinData.of(target).orElseThrow(() -> SINGLE_NO_SPIN.create(target.getName()));
        source.sendSuccess(new TranslationTextComponent(KEY + ".get.success", target.getDisplayName(),
                data.getLesson(), new TranslationTextComponent("rotp_spin.lesson." + data.getLesson()),
                data.getBallHits(), SpinConfig.LESSON2_BALL_HITS.get(),
                data.getHijacks(), SpinConfig.LESSON3_HIJACKS.get()), false);
        return data.getLesson();
    }
}
