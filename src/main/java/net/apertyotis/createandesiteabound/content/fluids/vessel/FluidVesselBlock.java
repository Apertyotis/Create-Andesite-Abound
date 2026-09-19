package net.apertyotis.createandesiteabound.content.fluids.vessel;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.apertyotis.createandesiteabound.AllBlockEntityType;
import net.apertyotis.createandesiteabound.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class FluidVesselBlock extends Block implements IBE<FluidVesselBlockEntity>, IWrenchable {

    public FluidVesselBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState());
    }

    @Override
    public Class<FluidVesselBlockEntity> getBlockEntityClass() {
        return FluidVesselBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidVesselBlockEntity> getBlockEntityType() {
        return AllBlockEntityType.FLUID_VESSEL.get();
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Block.box(4.75, 0, 4.75, 11.25, 16, 11.25);
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
    public @NotNull ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = AllBlocks.FLUID_VESSEL.asStack();
        if (level.getBlockEntity(pos) instanceof FluidVesselBlockEntity vessel) {
            FluidStack fluid = vessel.tank.getFluidInTank(0);
            if (!fluid.isEmpty()) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.put("Content", fluid.writeToNBT(new CompoundTag()));
            }
        }
        return stack;
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public @NotNull List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        ItemStack stack = AllBlocks.FLUID_VESSEL.asStack();
        if (pParams.getParameter(LootContextParams.BLOCK_ENTITY) instanceof FluidVesselBlockEntity vessel) {
            FluidStack fluid = vessel.tank.getFluidInTank(0);
            if (!fluid.isEmpty()) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.put("Content", fluid.writeToNBT(new CompoundTag()));
            }
        }
        return List.of(stack);
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel) {
            ItemStack stack = getCloneItemStack(level, pos, state);
            level.destroyBlock(pos, false);
            if (!player.getAbilities().instabuild)
                player.getInventory().placeItemBackInInventory(stack);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        FluidStack fluid = FluidVesselItem.getFluid(stack);
        if (!fluid.isEmpty()) {
            withBlockEntityDo(level, pos, be -> be.tank.setFluid(fluid));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown())
            return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof FluidVesselBlockEntity) {
            if (FluidVesselClickHandler.transferFluidWithBE(level, pos, player, hand) ||
                FluidVesselClickHandler.isFluidContainer(level, player.getItemInHand(hand)))
                // 即便没有成功转移液体，也阻止液体物品的进一步交互
                return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
