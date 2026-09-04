package net.apertyotis.createandesiteabound.mixin.create_connected;

import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlock;
import com.simibubi.create.content.redstone.DirectedDirectionalBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

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
}
