package net.apertyotis.createandesiteabound.mixin.create_connected;

import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.redstone.DirectedDirectionalBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.ParametersAreNonnullByDefault;

@Mixin(value = InventoryAccessPortBlock.class, remap = false)
public abstract class InventoryAccessPortBlockMixin extends DirectedDirectionalBlock {

    public InventoryAccessPortBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, world, pos, newState);
    }

    @WrapOperation(
        method = "getStateForPlacement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;)Lnet/minecraftforge/common/util/LazyOptional;"
        )
    )
    private LazyOptional<?> findTank(BlockEntity be, Capability<?> cap, Operation<LazyOptional<?>> original) {
        LazyOptional<?> itemCap = original.call(be, cap);
        if (!itemCap.isPresent()) {
            return be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        }
        return itemCap;
    }
}
