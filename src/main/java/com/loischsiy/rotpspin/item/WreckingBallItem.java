package com.loischsiy.rotpspin.item;

import java.util.List;

import javax.annotation.Nullable;

import com.loischsiy.rotpspin.config.SpinConfig;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Wrecking Ball: the royal guard version of the steel ball. Satellites hidden inside
 * the sphere fly out mid-flight; even a miss raises a disorienting shockwave.
 * Thrown, returned and repaired exactly like a steel ball (it is one).
 */
public class WreckingBallItem extends SteelBallItem {

    public WreckingBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (isChipped(stack)) {
            tooltip.add(new TranslationTextComponent("item.rotp_spin.steel_ball.damaged").withStyle(TextFormatting.RED));
        }
        tooltip.add(new TranslationTextComponent("item.rotp_spin.wrecking_ball.desc",
                SpinConfig.WRECKING_SATELLITES.get()).withStyle(TextFormatting.GRAY));
    }
}
