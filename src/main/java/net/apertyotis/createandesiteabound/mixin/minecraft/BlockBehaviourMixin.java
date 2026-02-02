package net.apertyotis.createandesiteabound.mixin.minecraft;

import net.apertyotis.createandesiteabound.Config;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(value = BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    // 对未实现旋转方法的方块应用启发式旋转
    // 自动检查常见方向属性并旋转
    @Inject(method = "rotate", at = @At("HEAD"), cancellable = true)
    private void heuristicRotate(BlockState p_60530_, Rotation p_60531_, CallbackInfoReturnable<BlockState> cir) {
        if (!Config.heuristic_rotation) return;

        // 常规朝向
        Collection<Property<?>> properties = p_60530_.getProperties();
        if (properties.contains(BlockStateProperties.FACING)) {
            p_60530_ = p_60530_.setValue(BlockStateProperties.FACING,
                    p_60531_.rotate(p_60530_.getValue(BlockStateProperties.FACING)));
        } else if (properties.contains(BlockStateProperties.HORIZONTAL_FACING)) {
            p_60530_ = p_60530_.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    p_60531_.rotate(p_60530_.getValue(BlockStateProperties.HORIZONTAL_FACING)));
        }

        // 轴朝向
        else if (properties.contains(BlockStateProperties.AXIS)) {
            p_60530_ = p_60530_.setValue(BlockStateProperties.AXIS,
                    p_60531_.rotate(Direction.fromAxisAndDirection(
                                    p_60530_.getValue(BlockStateProperties.AXIS),
                                    Direction.AxisDirection.POSITIVE))
                            .getAxis());
        } else if (properties.contains(BlockStateProperties.HORIZONTAL_AXIS)) {
            p_60530_ = p_60530_.setValue(BlockStateProperties.HORIZONTAL_AXIS,
                    p_60531_.rotate(Direction.fromAxisAndDirection(
                                    p_60530_.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                                    Direction.AxisDirection.POSITIVE))
                            .getAxis());
        }

        // 管道类朝向
        else if (properties.containsAll(
                List.of(BlockStateProperties.NORTH, BlockStateProperties.SOUTH,
                        BlockStateProperties.WEST, BlockStateProperties.EAST)
        )) {
            p_60530_ = switch (p_60531_) {
                case CLOCKWISE_90 -> p_60530_
                        .setValue(BlockStateProperties.NORTH, p_60530_.getValue(BlockStateProperties.WEST))
                        .setValue(BlockStateProperties.SOUTH, p_60530_.getValue(BlockStateProperties.EAST))
                        .setValue(BlockStateProperties.WEST, p_60530_.getValue(BlockStateProperties.SOUTH))
                        .setValue(BlockStateProperties.EAST, p_60530_.getValue(BlockStateProperties.NORTH));
                case CLOCKWISE_180 -> p_60530_
                        .setValue(BlockStateProperties.NORTH, p_60530_.getValue(BlockStateProperties.SOUTH))
                        .setValue(BlockStateProperties.SOUTH, p_60530_.getValue(BlockStateProperties.NORTH))
                        .setValue(BlockStateProperties.WEST, p_60530_.getValue(BlockStateProperties.EAST))
                        .setValue(BlockStateProperties.EAST, p_60530_.getValue(BlockStateProperties.WEST));
                case COUNTERCLOCKWISE_90 -> p_60530_
                        .setValue(BlockStateProperties.NORTH, p_60530_.getValue(BlockStateProperties.EAST))
                        .setValue(BlockStateProperties.SOUTH, p_60530_.getValue(BlockStateProperties.WEST))
                        .setValue(BlockStateProperties.WEST, p_60530_.getValue(BlockStateProperties.NORTH))
                        .setValue(BlockStateProperties.EAST, p_60530_.getValue(BlockStateProperties.SOUTH));
                case NONE -> p_60530_;
            };
        }
        cir.setReturnValue(p_60530_);
    }

    // 对未实现镜像方法的方块应用启发式镜像
    // 自动检查常见方向属性并镜像
    @Inject(method = "mirror", at = @At("HEAD"), cancellable = true)
    private void heuristicMirror(BlockState p_60528_, Mirror p_60529_, CallbackInfoReturnable<BlockState> cir) {
        if (!Config.heuristic_rotation) return;

        Collection<Property<?>> properties = p_60528_.getProperties();
        // 常规朝向
        if (properties.contains(BlockStateProperties.FACING)) {
            if (p_60529_.getRotation(p_60528_.getValue(BlockStateProperties.FACING)) == Rotation.CLOCKWISE_180) {
                p_60528_ = p_60528_.setValue(BlockStateProperties.FACING,
                        Rotation.CLOCKWISE_180.rotate(p_60528_.getValue(BlockStateProperties.FACING)));
            }
        } else if (properties.contains(BlockStateProperties.HORIZONTAL_FACING)) {
            if (p_60529_.getRotation(p_60528_.getValue(BlockStateProperties.HORIZONTAL_FACING)) == Rotation.CLOCKWISE_180) {
                p_60528_ = p_60528_.setValue(BlockStateProperties.HORIZONTAL_FACING,
                        Rotation.CLOCKWISE_180.rotate(p_60528_.getValue(BlockStateProperties.HORIZONTAL_FACING)));
            }
        }

        // 轴朝向不用镜像

        // 管道类朝向
        else if (properties.containsAll(
                List.of(BlockStateProperties.NORTH, BlockStateProperties.SOUTH,
                        BlockStateProperties.WEST, BlockStateProperties.EAST)
        )) {
            p_60528_ = switch (p_60529_) {
                case LEFT_RIGHT -> p_60528_
                        .setValue(BlockStateProperties.NORTH, p_60528_.getValue(BlockStateProperties.SOUTH))
                        .setValue(BlockStateProperties.SOUTH, p_60528_.getValue(BlockStateProperties.NORTH));
                case FRONT_BACK -> p_60528_
                        .setValue(BlockStateProperties.WEST, p_60528_.getValue(BlockStateProperties.EAST))
                        .setValue(BlockStateProperties.EAST, p_60528_.getValue(BlockStateProperties.WEST));
                case NONE -> p_60528_;
            };
        }
        cir.setReturnValue(p_60528_);
    }
}
