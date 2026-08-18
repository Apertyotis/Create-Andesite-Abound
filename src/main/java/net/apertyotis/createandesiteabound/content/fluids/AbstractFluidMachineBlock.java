package net.apertyotis.createandesiteabound.content.fluids;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import net.apertyotis.createandesiteabound.foundation.CircularArray;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public abstract class AbstractFluidMachineBlock<T extends BlockEntity> extends HorizontalDirectionalBlock implements IBE<T>, IWrenchable {

    public AbstractFluidMachineBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        if (context.getPlayer() != null)
            state = state.setValue(FACING, context.getPlayer().getDirection());
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, world, pos, newState);
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> list = super.getDrops(state, params);
        BlockEntity be = params.getParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof AbstractFluidMachineBlockEntity fluidMachine) {
            CircularArray<ItemStack> items = fluidMachine.inventory.items;
            for (int i = 0; i < items.size(); i++)
                list.add(items.get(i));
        }
        return list;
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block other, BlockPos otherPos, boolean moving) {
        InvManipulationBehaviour invMan = BlockEntityBehaviour.get(level, pos, InvManipulationBehaviour.TYPE);
        if (invMan != null)
            invMan.onNeighborChanged(otherPos);
        TankManipulationBehaviour tankMan = BlockEntityBehaviour.get(level, pos, TankManipulationBehaviour.OBSERVE);
        if (tankMan != null)
            tankMan.onNeighborChanged(otherPos);
    }
}
