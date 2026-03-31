package net.apertyotis.createandesiteabound.mixin.create.logistics;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.simibubi.create.foundation.utility.IPartialSafeNBT;
import net.apertyotis.createandesiteabound.foundation.SameSizeCombinedInvWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemVaultBlockEntity.class, remap = false)
public abstract class ItemVaultBlockEntityMixin implements IPartialSafeNBT {

    /**
     * 修复蓝图打印单格保险库容量为0的问题<br>
     * 详见 Create PR<a href="https://github.com/Creators-of-Create/Create/pull/10084">#10084</a>
     */
    @Override
    public void writeSafe(CompoundTag compound) {
        ItemVaultBlockEntity vault = (ItemVaultBlockEntity)(Object) this;
        if (vault.isController()) {
            compound.putInt("Size", vault.getWidth());
            compound.putInt("Length", vault.getHeight());
        }
    }

    /**
     * 移植 6.0 版本的物品包装类，用于优化大型保险库的存取性能<br>
     * 详见 Create Commit <a href="https://github.com/Creators-of-Create/Create/commit/b97a81df7e28228cfa36f305a18f06f14be849d7">b97a81d</a>
     */
    @WrapOperation(
            method = "initCapability",
            at = @At(value = "NEW", target = "Lnet/minecraftforge/items/wrapper/CombinedInvWrapper;")
    )
    private CombinedInvWrapper createSameSizeCombinedInvWrapper(
            IItemHandlerModifiable[] itemHandler, Operation<CombinedInvWrapper> original
    ) {
        return SameSizeCombinedInvWrapper.create(itemHandler);
    }

    /**
     * 移植 6.0 版本的更新比较器方法，减少大型保险库比较器更新的次数<br>
     * 详见 Create Commit <a href="https://github.com/Creators-of-Create/Create/commit/9476f42da8adc1b91ba05bb2d67d939ac809e2aa">9476f42</a><br>
     * <br>
     * 并修复保险库内容变化时不自动保存的问题<br>
     * 详见 Create Commit <a href="https://github.com/Creators-of-Create/Create/commit/e67d8da904c650255eaeb8c86d39ad2885d4afe3">e67d8da</a>
     */
    @Inject(method = "updateComparators", at = @At("HEAD"), cancellable = true)
    private void updateComparatorsForSurface(CallbackInfo ci) {
        ci.cancel();

        ItemVaultBlockEntity vault = (ItemVaultBlockEntity)(Object) this;
        ItemVaultBlockEntity controller = vault.getControllerBE();
        Level level = vault.getLevel();
        if (controller == null || level == null)
            return;

        // 由于从匿名内部类Mixin访问外部类困难，将更新逻辑放在此处
        level.blockEntityChanged(vault.getBlockPos());

        BlockPos pos = controller.getBlockPos();
        level.blockEntityChanged(pos);

        int radius = controller.getWidth();
        int length = controller.getHeight();
        Direction.Axis axis = controller.getMainConnectionAxis();
        int zMax = (axis == Direction.Axis.X ? radius : length);
        int xMax = (axis == Direction.Axis.Z ? radius : length);

        // Mutable position we'll use for the blocks we poke updates at.
        BlockPos.MutableBlockPos updatePos = new BlockPos.MutableBlockPos();
        // Mutable position we'll set to be the vault block next to the update position.
        BlockPos.MutableBlockPos provokingPos = new BlockPos.MutableBlockPos();

        for (int y = 0; y < radius; y++) {
            for (int z = 0; z < zMax; z++) {
                for (int x = 0; x < xMax; x++) {
                    // Emulate the effect of this line, but only for blocks along the surface of the vault:
                    // level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
                    // That method pokes all 6 directions in order. We want to preserve the update order
                    // but skip the wasted work of checking other blocks that are part of this vault.

                    var sectionX = SectionPos.blockToSectionCoord(pos.getX() + x);
                    var sectionZ = SectionPos.blockToSectionCoord(pos.getZ() + z);
                    if (!level.hasChunk(sectionX, sectionZ)) {
                        continue;
                    }
                    provokingPos.setWithOffset(pos, x, y, z);

                    // Technically all this work is wasted for the inner blocks of a long 3x3 vault, but
                    // this is fast enough and relatively simple.
                    Block provokingBlock = level.getBlockState(provokingPos).getBlock();

                    // The 6 calls below should match the order of Direction.values().
                    if (y == 0) {
                        caa$updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.DOWN);
                    }
                    if (y == radius - 1) {
                        caa$updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.UP);
                    }
                    if (z == 0) {
                        caa$updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.NORTH);
                    }
                    if (z == zMax - 1) {
                        caa$updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.SOUTH);
                    }
                    if (x == 0) {
                        caa$updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.WEST);
                    }
                    if (x == xMax - 1) {
                        caa$updateComparatorsInner(level, provokingBlock, provokingPos, updatePos, Direction.EAST);
                    }
                }
            }
        }
    }

    /**
     * See {@link Level#updateNeighbourForOutputSignal(BlockPos, Block)}.
     */
    @Unique
    private static void caa$updateComparatorsInner(Level level, Block provokingBlock, BlockPos provokingPos, BlockPos.MutableBlockPos updatePos, Direction direction) {
        updatePos.setWithOffset(provokingPos, direction);

        var sectionX = SectionPos.blockToSectionCoord(updatePos.getX());
        var sectionZ = SectionPos.blockToSectionCoord(updatePos.getZ());
        if (!level.hasChunk(sectionX, sectionZ)) {
            return;
        }

        BlockState blockstate = level.getBlockState(updatePos);
        blockstate.onNeighborChange(level, updatePos, provokingPos);
        if (blockstate.isRedstoneConductor(level, updatePos)) {
            updatePos.move(direction);
            blockstate = level.getBlockState(updatePos);
            if (blockstate.getWeakChanges(level, updatePos)) {
                level.neighborChanged(blockstate, updatePos, provokingBlock, provokingPos, false);
            }
        }
    }
}
