package net.apertyotis.createandesiteabound.mixin.design_decor;

import com.mangomilk.design_decor.blocks.railings.RailingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

// 使用正确的方式设定栏杆的掉落物
@Mixin(value = RailingBlock.class)
public abstract class RailingBlockMixin extends Block {
    public RailingBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(method = "playerDestroy", at = @At("HEAD"), cancellable = true)
    private void superPlayerDestroy(
            Level level, Player player, BlockPos pos,
            BlockState state, BlockEntity entity, ItemStack stack, CallbackInfo ci
    ) {
        super.playerDestroy(level, player, pos, state, entity, stack);
        ci.cancel();
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public @NotNull List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        int numero = 0;
        if (pState.getValue(RailingBlock.NORTH))
            ++numero;
        if (pState.getValue(RailingBlock.SOUTH))
            ++numero;
        if (pState.getValue(RailingBlock.EAST))
            ++numero;
        if (pState.getValue(RailingBlock.WEST))
            ++numero;

        ItemStack stack = new ItemStack(pState.getBlock().asItem(), numero);
        return Collections.singletonList(stack);
    }
}
