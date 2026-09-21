package net.apertyotis.createandesiteabound.content.trinklet;

import net.apertyotis.createandesiteabound.AllItems;
import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.curios.Curios;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BottledGhost extends Item {
    public BottledGhost(Properties pProperties) {
        super(pProperties);
    }

    public static boolean isFlyingNoclip(Player player) {
        if (player == null || !player.getAbilities().flying)
            return false;
        if (AllItems.BOTTLED_GHOST.isIn(player.getMainHandItem()) ||
            AllItems.BOTTLED_GHOST.isIn(player.getOffhandItem()))
            return true;
        return Mods.Curios.runIfInstalled(() -> () -> Curios.isWearingBottledGhost(player)).orElse(false);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack pStack) {
        return Component.translatable("item.createandesiteabound.bottled_ghost").withStyle(ChatFormatting.GREEN);
    }
}
