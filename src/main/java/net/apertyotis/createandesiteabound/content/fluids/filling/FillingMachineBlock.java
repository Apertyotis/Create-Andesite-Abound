package net.apertyotis.createandesiteabound.content.fluids.filling;

import net.apertyotis.createandesiteabound.AllBlockEntityType;
import net.apertyotis.createandesiteabound.content.fluids.AbstractFluidMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.ParametersAreNonnullByDefault;

public class FillingMachineBlock extends AbstractFluidMachineBlock<FillingMachineBlockEntity> {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public FillingMachineBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (level.hasNeighborSignal(pos))
            level.setBlock(pos, state.setValue(POWERED, true), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public Class<FillingMachineBlockEntity> getBlockEntityClass() {
        return FillingMachineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FillingMachineBlockEntity> getBlockEntityType() {
        return AllBlockEntityType.FILLING_MACHINE.get();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return !state.getValue(FACING).equals(side);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block other, BlockPos otherPos, boolean moving) {
        super.neighborChanged(state, level, pos, other, otherPos, moving);
        boolean power = level.hasNeighborSignal(pos);
        if (state.getValue(POWERED) != power) {
            level.setBlock(pos, state.setValue(POWERED, power), 3);
            if (power)
                withBlockEntityDo(level, pos, be -> be.setCooldown(0));
        }
    }
}
