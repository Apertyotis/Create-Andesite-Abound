package net.apertyotis.createandesiteabound.content.belt;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BeltValueBoxTransform extends ValueBoxTransform.Sided {

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        try {
            BeltSlope slope = state.getValue(BeltBlock.SLOPE);
            BeltPart part = state.getValue(BeltBlock.PART);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            switch (slope) {
                case HORIZONTAL -> {
                    return VecHelper.voxelSpace(8, 12.5, 8);
                }
                case UPWARD, DOWNWARD -> {
                    boolean magic = facing.getAxisDirection() == AxisDirection.POSITIVE ^ slope == BeltSlope.UPWARD;
                    boolean top = (part == BeltPart.START && slope == BeltSlope.DOWNWARD) ||
                        (part == BeltPart.END && slope == BeltSlope.UPWARD);
                    boolean bottom = (part == BeltPart.START && slope == BeltSlope.UPWARD) ||
                        (part == BeltPart.END && slope == BeltSlope.DOWNWARD);
                    if (top) {
                        if (facing.getAxis() == Axis.Z)
                            return VecHelper.voxelSpace(8, 12.5, magic ? 6 : 10);
                        else
                            return VecHelper.voxelSpace(magic ? 6 : 10, 12.5, 8);
                    } else if (bottom) {
                        if (facing.getAxis() == Axis.Z)
                            return VecHelper.voxelSpace(8, 15, magic ? 7.5 : 8.5);
                        else
                            return VecHelper.voxelSpace(magic ? 7.5 : 8.5, 15, 8);
                    } else {
                        return VecHelper.voxelSpace(8, 14.5, 8);
                    }
                }
                case VERTICAL, SIDEWAYS -> {
                    return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 8, 12.5),
                        AngleHelper.horizontalAngle(getSide()), Axis.Y);
                }
            }
        } catch (IllegalArgumentException ignored) {}
        return Vec3.ZERO;
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        try {
            BeltSlope slope = state.getValue(BeltBlock.SLOPE);
            BeltPart part = state.getValue(BeltBlock.PART);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            switch (slope) {
                case HORIZONTAL -> TransformStack.cast(ms).rotateY(180).rotateX(90);
                case VERTICAL, SIDEWAYS -> {
                    float yRot = switch (getSide()) {
                        case SOUTH -> 180;
                        case WEST -> 90;
                        case EAST -> 270;
                        default -> 0;
                    };
                    TransformStack.cast(ms).rotateY(yRot);
                }
                case UPWARD, DOWNWARD -> {
                    boolean magic = facing.getAxisDirection() == AxisDirection.POSITIVE ^ slope == BeltSlope.UPWARD;
                    boolean top = (part == BeltPart.START && slope == BeltSlope.DOWNWARD) ||
                        (part == BeltPart.END && slope == BeltSlope.UPWARD);
                    if (facing.getAxis() == Axis.Z)
                        TransformStack.cast(ms).rotateY(magic ? 180 : 0);
                    else
                        TransformStack.cast(ms).rotateY(magic ? 270 : 90);
                    TransformStack.cast(ms).rotateX(top ? 90 : 45);
                }
            }
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        try {
            BeltSlope slope = state.getValue(BeltBlock.SLOPE);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            switch (slope) {
                case HORIZONTAL, UPWARD, DOWNWARD -> {
                    if (direction == Direction.UP)
                        return true;
                    else if (Minecraft.getInstance().hitResult instanceof BlockHitResult hit) {
                        return hit.getLocation().y - hit.getBlockPos().getY() >= 10 / 16f;
                    } else {
                        return false;
                    }
                }
                case VERTICAL -> {
                    return direction.getAxis().isHorizontal() && direction.getAxis() == facing.getAxis();
                }
                case SIDEWAYS -> {
                    return direction.getAxis().isHorizontal() && direction.getAxis() != facing.getAxis();
                }
            }
        } catch (IllegalArgumentException ignored) {}
        return false;
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }
}
