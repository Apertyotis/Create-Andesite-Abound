package net.apertyotis.createandesiteabound.mixin.create_connected;

import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlockEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryAccessPortBlockEntity.class, remap = false)
public abstract class InventoryAccessPortBlockEntityMixin extends SmartBlockEntity {

    @Shadow
    protected abstract void initCapability();

    @Shadow
    protected LazyOptional<IItemHandler> itemCapability;

    public InventoryAccessPortBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initCapabilityOnce(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        initCapability();
    }

    @WrapOperation(
        method = "getCapability",
        at = @At(
            value = "INVOKE",
            target = "Lcom/hlysine/create_connected/content/inventoryaccessport/InventoryAccessPortBlockEntity;initCapability()V"
        )
    )
    private void removeDuplicatedInstance(InventoryAccessPortBlockEntity instance, Operation<Void> original) {}

    @Override
    public void invalidate() {
        super.invalidate();
        itemCapability.invalidate();
    }
}
