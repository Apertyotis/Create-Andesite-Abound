package net.apertyotis.createandesiteabound.mixin.minecraft.noclip;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.apertyotis.createandesiteabound.content.trinklet.BottledGhost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @WrapOperation(
        method = "canPlace",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;isUnobstructed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Z"
        )
    )
    private boolean placeNoclip(
        Level level, BlockState state, BlockPos pos, CollisionContext collision, Operation<Boolean> original,
        @Local(argsOnly = true) BlockPlaceContext context
    ) {
        Player player = context.getPlayer();
        if (BottledGhost.isFlyingNoclip(player)) {
            VoxelShape voxelShape = state.getCollisionShape(level, pos, collision);
            return voxelShape.isEmpty() || level.isUnobstructed(player, voxelShape.move(pos.getX(), pos.getY(), pos.getZ()));
        }
        return original.call(level, state, pos, collision);
    }
}
