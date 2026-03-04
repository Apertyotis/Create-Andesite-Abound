package net.apertyotis.createandesiteabound.content.radar;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Iterate;
import net.apertyotis.createandesiteabound.AllBlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class RedstoneRadarBlock extends Block implements IBE<RedstoneRadarBlockEntity>, IWrenchable {

    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final IntegerProperty INPUT = IntegerProperty.create("input", 0, 15);
    public static final IntegerProperty OUTPUT = IntegerProperty.create("output", 0, 15);
    public static final BooleanProperty FORCELOAD = BooleanProperty.create("forceload");

    public RedstoneRadarBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(INPUT, 0)
                .setValue(OUTPUT, 0)
                .setValue(FORCELOAD, false)
        );
    }

    @Override
    public Class<RedstoneRadarBlockEntity> getBlockEntityClass() {
        return RedstoneRadarBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RedstoneRadarBlockEntity> getBlockEntityType() {
        return AllBlockEntityType.REDSTONE_RADAR.get();
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Block.box(1, 0, 1, 15, 8, 15);
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (world.isClientSide())
            return;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("TargetPos") || !tag.contains("TargetDimension"))
            return;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof RedstoneRadarBlockEntity rrbe) {
            BlockPos targetPos = NbtUtils.readBlockPos(tag.getCompound("TargetPos"));
            ResourceLocation id = new ResourceLocation(tag.getString("TargetDimension"));
            ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, id);
            rrbe.setTarget(targetDimension, targetPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, world, pos, newState);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return isOutput(state, side.getOpposite()) ? state.getValue(OUTPUT) : 0;
    }

    @Override
    public void neighborChanged(
            BlockState state, Level world, BlockPos pos,
            Block otherBlock, BlockPos neighborPos, boolean isMoving
    ) {
        updateInputSignal(state, world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(NORTH, EAST, SOUTH, WEST, INPUT, OUTPUT, FORCELOAD));
    }

    @Override
    public @NotNull InteractionResult use(
            BlockState state, Level world, BlockPos pos,
            Player player, InteractionHand handIn, BlockHitResult hit
    ) {
        ItemStack item = player.getItemInHand(handIn);
        if (!item.hasTag() || !(world.getBlockEntity(pos) instanceof RedstoneRadarBlockEntity be))
            return InteractionResult.PASS;

        CompoundTag tag = item.getTag();
        if (tag != null && tag.contains("position") && tag.contains("dimension")) {
            BlockPos position;
            ResourceKey<Level> dimension;
            try {
                if (tag.getTagType("position") != Tag.TAG_LIST)
                    return InteractionResult.PASS;
                ListTag posTag = (ListTag) tag.get("position");
                if (posTag == null || posTag.size() != 3)
                    return InteractionResult.PASS;
                position = switch (posTag.getElementType()) {
                    case Tag.TAG_INT ->
                            new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
                    case Tag.TAG_FLOAT ->
                            BlockPos.containing(posTag.getFloat(0), posTag.getFloat(1), posTag.getFloat(2));
                    case Tag.TAG_DOUBLE ->
                            BlockPos.containing(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2));
                    default -> null;
                };
                if (position == null)
                    return InteractionResult.PASS;

                if (tag.getTagType("dimension") != Tag.TAG_STRING)
                    return InteractionResult.PASS;
                ResourceLocation id = new ResourceLocation(tag.getString("dimension"));
                dimension = ResourceKey.create(Registries.DIMENSION, id);
            } catch (ClassCastException ignored) {
                return InteractionResult.PASS;
            }

            be.setTarget(dimension, position);
            world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.25f, 2f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        if (world.isClientSide())
            return InteractionResult.SUCCESS;

        if (side == Direction.DOWN) {
            return InteractionResult.PASS;
        } else if (side == Direction.UP) {
            world.setBlock(pos, state.cycle(FORCELOAD), 2);
            if (world.getBlockEntity(pos) instanceof RedstoneRadarBlockEntity be)
                be.scheduleUpdate();
            playRotateSound(world, context.getClickedPos());
            return InteractionResult.SUCCESS;
        } else {
            BooleanProperty property = asProperty(side);
            if (property != null) {
                BlockState newState = state.cycle(property);
                world.setBlock(pos, newState, 3);
                updateInputSignal(newState, world, pos);
                playRotateSound(world, context.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    static private BooleanProperty asProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> null;
        };
    }

    static public boolean isOutput(BlockState state, Direction side) {
        BooleanProperty property = asProperty(side);
        if (property != null)
            return state.getValue(property);
        else
            return false;
    }

    static public void updateInputSignal(BlockState state, Level world, BlockPos pos) {
        int shouldInput = 0;

        for (Direction d: Iterate.horizontalDirections) {
            if (isOutput(state, d))
                continue;
            shouldInput = Math.max(world.getSignal(pos.relative(d), d), shouldInput);
        }

        shouldInput = Math.min(shouldInput, 15);

        if (shouldInput != state.getValue(INPUT)) {
            world.setBlock(pos, state.setValue(INPUT, shouldInput), 2);
            if (world.getBlockEntity(pos) instanceof RedstoneRadarBlockEntity be) {
                be.scheduleUpdate();
            }
        }
    }
}
