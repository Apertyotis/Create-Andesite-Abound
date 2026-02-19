package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.BeltTunnelInteractionHandler;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;

@Mixin(value = BeltTunnelInteractionHandler.class, remap = false)
public abstract class BeltTunnelInteractionHandlerMixin {
    /**
     * 解决正向传送带上第一个安山隧道失效的问题<br>
     * 详见 Create PR <a href="https://github.com/Creators-of-Create/Create/pull/9967">#9967</a>
     */
    @ModifyVariable(
            method = "flapTunnelsAndCheckIfStuck",
            at = @At(value = "STORE"),
            name = "currentSegment"
    )
    private static int redirectCurrentSegment(
            int currentSegment,
            @Local(argsOnly = true) BeltInventory beltInventory,
            @Local(argsOnly = true) TransportedItemStack current)
    {
        if (((BeltInventoryAccessor) beltInventory).isPositive() && current.beltPosition <= .0f)
            return -1;
        return currentSegment;
    }

    /**
     * 修复安山隧道向两侧输出的方向性问题<br>
     * 详见 issue <a href="https://github.com/Creators-of-Create/Create/issues/9682">#9682</a>
     */
    @SuppressWarnings("SameReturnValue")
    @ModifyVariable(
            method = "flapTunnelsAndCheckIfStuck",
            at = @At(
                    value = "LOAD",
                    ordinal = 0
            ),
            name = "nextTunnel",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;setChanged()V"
                    ),
                    to = @At(
                        value = "INVOKE",
                        target = "Lcom/simibubi/create/content/logistics/tunnel/BeltTunnelBlockEntity;" +
                                "getBlockState()Lnet/minecraft/world/level/block/state/BlockState;"
                    )
            )
    )
    private static BeltTunnelBlockEntity redirectAndesiteTunnelCheck(
            BeltTunnelBlockEntity nextTunnel,
            @Local(argsOnly = true) BeltInventory beltInventory,
            @Local(argsOnly = true) TransportedItemStack current,
            @Local(argsOnly = true) float nextOffset,
            @Cancellable CallbackInfoReturnable<Boolean> cir)
    {
        if (nextTunnel == null) return null;

        int upcomingSegment = (int) nextOffset;
        BeltBlockEntity belt = ((BeltInventoryAccessor) beltInventory).getBelt();
        Direction movementFacing = belt.getMovementFacing();
        Level world = belt.getLevel();
        boolean onServer = (world != null && !world.isClientSide) || belt.isVirtual();
        BlockState blockState = nextTunnel.getBlockState();

        if (current.stack.getCount() > 1 && AllBlocks.ANDESITE_TUNNEL.has(blockState)
                && BeltTunnelBlock.isJunction(blockState)
                && movementFacing.getAxis() == blockState.getValue(BeltTunnelBlock.HORIZONTAL_AXIS))
        {
            EnumMap<Direction, DirectBeltInputBehaviour> outputs = new EnumMap<>(Direction.class);
            for (Direction d: Iterate.horizontalDirections) {
                if (d.getAxis() == blockState.getValue(BeltTunnelBlock.HORIZONTAL_AXIS))
                    continue;
                if (!nextTunnel.flaps.containsKey(d))
                    continue;
                BlockPos outPos = nextTunnel.getBlockPos().below().relative(d);
                if (world == null || !world.isLoaded(outPos)) {
                    cir.setReturnValue(true);
                    return null;
                }
                DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(world, outPos, DirectBeltInputBehaviour.TYPE);
                if (behaviour == null || !behaviour.canInsertFromSide(d))
                    continue;

                ItemStack toInsert = ItemHandlerHelper.copyStackWithSize(current.stack, 1);
                if (!behaviour.handleInsertion(toInsert, d, true).isEmpty()) {
                    cir.setReturnValue(true);
                    return null;
                }
                outputs.put(d, behaviour);

                if (outputs.size() + 1 >= current.stack.getCount())
                    break;
            }

            for (var entry: outputs.entrySet()) {
                ItemStack toInsert = ItemHandlerHelper.copyStackWithSize(current.stack, 1);
                if (!entry.getValue().handleInsertion(toInsert, entry.getKey(), false).isEmpty()) {
                    cir.setReturnValue(true);
                    return null;
                }
                if (onServer)
                    BeltTunnelInteractionHandler.flapTunnel(beltInventory, upcomingSegment, entry.getKey(), false);

                current.stack.shrink(1);
                belt.sendData();
                if (current.stack.getCount() <= 1)
                    break;
            }
        }
        return null;
    }
}
