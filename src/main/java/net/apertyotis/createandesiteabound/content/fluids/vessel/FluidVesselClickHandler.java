package net.apertyotis.createandesiteabound.content.fluids.vessel;

import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.apertyotis.createandesiteabound.AllBlocks;
import net.apertyotis.createandesiteabound.AllPackets;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

import static net.minecraftforge.fluids.FluidUtil.tryEmptyContainerAndStow;
import static net.minecraftforge.fluids.FluidUtil.tryFillContainerAndStow;

/**
 * 处理流体容器的方块中键复制行为和物品右键交互行为
 * @see FluidVesselBlock#use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)
 */
@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID)
public class FluidVesselClickHandler {
    public static boolean onMiddleClick(int button, boolean pressed) {
        if (button != 2 || !pressed)
            return false;
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;
        if (level == null || player == null || !player.getMainHandItem().isEmpty() || player.getAbilities().instabuild)
            return false;

        BlockHitResult hit = RaycastHelper.rayTraceRange(level, player, player.getBlockReach());
        if (hit.getType() == HitResult.Type.BLOCK &&
            AllBlocks.FLUID_VESSEL.has(level.getBlockState(hit.getBlockPos()))
        ) {
            AllPackets.getChannel().sendToServer(new GetFreeFluidVesselPacket(-1));
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack copyItem = event.getItemStack().copyWithCount(1);
        IFluidHandlerItem handler = copyItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (player.isShiftKeyDown() || !AllBlocks.FLUID_VESSEL.isIn(copyItem) || handler == null)
            return;

        if (handleFluidState(level, player, event.getHand(), copyItem, handler)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack copyItem = event.getItemStack().copyWithCount(1);
        if (player.isShiftKeyDown() || !AllBlocks.FLUID_VESSEL.isIn(copyItem))
            return;
        IFluidHandlerItem handler = copyItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (handler == null)
            return;

        if (handleFluidState(level, player, event.getHand(), copyItem, handler)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ItemDrainBlockEntity) {
            if (transferFluidWithBE(level, pos, player, event.getHand())) {
                // 分液池不为空，取液
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            // 分液池为空，则由分液池处理右键分液行为
            return;
        }
        if (be != null && be.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            // 对一般流体容器的处理延后
            return;
        }

        if (handlePlaceFluid(level, pos, event.getFace(), player, event.getHand(), copyItem, handler)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    public static boolean handleFluidState(
        Level level, Player player, InteractionHand hand, ItemStack copyItem, IFluidHandlerItem handler
    ) {
        BlockHitResult hit = findFluidState(level, player);
        if (hit.getType() == HitResult.Type.MISS)
            return false;
        // 置物台是含水方块，不要处理它
        if (level.getBlockEntity(hit.getBlockPos()) instanceof DepotBlockEntity && hit.getDirection() == Direction.UP)
            return false;

        FluidStack fluidStack = handler.getFluidInTank(0);
        int amount = fluidStack.getAmount();
        int space = handler.getTankCapacity(0) - amount;
        BlockState blockState = level.getBlockState(hit.getBlockPos());
        FluidState fluidState = blockState.getFluidState();
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            if (!fluidStack.isEmpty() && fluidStack.getFluid() != Fluids.WATER)
                return false;
            if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
                if (space >= 1000) {
                    handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
                    blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
                    level.setBlock(hit.getBlockPos(), blockState, 3);
                    level.scheduleTick(hit.getBlockPos(), Fluids.WATER, Fluids.WATER.getTickDelay(level));
                    playBucketSound(player, true);
                    placeResultItem(player, hand, copyItem);
                } else if (amount >= 1000) {
                    handler.drain(1000, FluidAction.EXECUTE);
                    playBucketSound(player, false);
                    placeResultItem(player, hand, copyItem);
                }
            } else if (amount >= 1000) {
                handler.drain(1000, FluidAction.EXECUTE);
                playBucketSound(player, false);
                placeResultItem(player, hand, copyItem);
                blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                level.setBlock(hit.getBlockPos(), blockState, 3);
                level.scheduleTick(hit.getBlockPos(), Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
            return true;
        } else if (!fluidState.isEmpty() && fluidState.isSource() &&
            (fluidStack.isEmpty() || (fluidStack.getFluid() == fluidState.getType() && space >= 1000))
        ) {
            handler.fill(new FluidStack(fluidState.getType(), 1000), FluidAction.EXECUTE);
            level.setBlock(hit.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            playBucketSound(player, true);
            placeResultItem(player, hand, copyItem);
            return true;
        }
        return false;
    }

    public static boolean handlePlaceFluid(
        Level level, BlockPos pos, Direction side, Player player, InteractionHand hand,
        ItemStack copyItem, IFluidHandlerItem handler
    ) {
        FluidStack fluidStack = handler.getFluidInTank(0);
        int amount = fluidStack.getAmount();
        if (amount < 1000)
            return false;

        BlockState blockState = level.getBlockState(pos);
        FluidState toPlace = fluidStack.getFluid().defaultFluidState();
        if (toPlace.isEmpty()) {
            handler.drain(1000, FluidAction.EXECUTE);
            playBucketSound(player, false);
            placeResultItem(player, hand, copyItem);
            return true;
        }
        if (!blockState.canBeReplaced(fluidStack.getFluid()) && side != null) {
            pos = pos.relative(side);
            blockState = level.getBlockState(pos);
        }
        if (blockState.canBeReplaced(fluidStack.getFluid())) {
            handler.drain(1000, FluidAction.EXECUTE);
            level.setBlock(pos, toPlace.createLegacyBlock(), 3);
            playBucketSound(player, false);
            placeResultItem(player, hand, copyItem);
            return true;
        }
        return false;
    }

    public static BlockHitResult findFluidState(Level level, Player player) {
        double range = player.getBlockReach();
        Vec3 origin = RaycastHelper.getTraceOrigin(player);
        Vec3 target = RaycastHelper.getTraceTarget(player, range, origin);
        ClipContext context = new ClipContext(origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, player);
        return level.clip(context);
    }

    public static void playBucketSound(Player player, boolean isFilling) {
        player.playSound(isFilling ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY, 1f, 1f);
    }

    public static void placeResultItem(Player player, InteractionHand hand, ItemStack result) {
        if (player.getAbilities().instabuild)
            return;
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getCount() > 1) {
            heldItem.shrink(1);
            player.getInventory().placeItemBackInInventory(result);
        } else {
            player.setItemInHand(hand, result);
        }
    }

    public static boolean transferFluidWithBE(Level level, BlockPos pos, Player player, InteractionHand hand) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null)
            return false;
        ItemStack heldItem = player.getItemInHand(hand);
        IFluidHandler fluidHandler = be.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve().orElse(null);
        IItemHandler inventory = player.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
        if (heldItem.isEmpty() || fluidHandler == null || inventory == null)
            return false;

        FluidActionResult result = tryEmptyContainerAndStow(heldItem, fluidHandler, inventory, Integer.MAX_VALUE, player, true);
        if (!result.isSuccess()) {
            result = tryFillContainerAndStow(heldItem, fluidHandler, inventory, Integer.MAX_VALUE, player, true);
        }

        if (result.isSuccess()) {
            player.setItemInHand(hand, result.getResult());
            return true;
        }

        return false;
    }

    public static boolean isFluidContainer(Level level, ItemStack stack) {
        return GenericItemEmptying.canItemBeEmptied(level, stack) || GenericItemFilling.canItemBeFilled(level, stack);
    }
}
