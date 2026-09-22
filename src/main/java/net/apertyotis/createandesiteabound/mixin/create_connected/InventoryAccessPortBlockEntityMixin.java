package net.apertyotis.createandesiteabound.mixin.create_connected;

import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlockEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import net.apertyotis.createandesiteabound.foundation.TankAccessHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = InventoryAccessPortBlockEntity.class, remap = false)
public abstract class InventoryAccessPortBlockEntityMixin extends SmartBlockEntity {

    @Shadow
    protected abstract void initCapability();

    @Shadow
    protected LazyOptional<IItemHandler> itemCapability;

    @Shadow
    private boolean powered;

    @Unique
    private TankManipulationBehaviour caa$observedTank;

    @Unique
    private LazyOptional<IFluidHandler> caa$tankCapability;

    public InventoryAccessPortBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initCapabilityOnce(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        initCapability();
        caa$tankCapability = LazyOptional.of(() -> new TankAccessHandler(() -> {
            if (powered)
                return null;
            IFluidHandler handler = caa$observedTank.getInventory();
            return handler instanceof TankAccessHandler ? null : handler;
        }));
    }

    @Inject(method = "addBehaviours", at = @At("TAIL"))
    private void addObservedTank(
        List<BlockEntityBehaviour> behaviours, CallbackInfo ci,
        @Local(name = "towardBlockFacing") CapManipulationBehaviourBase.InterfaceProvider towardBlockFacing
    ) {
        caa$observedTank = new TankManipulationBehaviour(this, towardBlockFacing);
        behaviours.add(caa$observedTank);
    }

    @Inject(method = "isAttached", at = @At("RETURN"), cancellable = true)
    private void hasAttachedTank(CallbackInfoReturnable<Boolean> cir) {
        boolean result = cir.getReturnValue() || (!powered && caa$observedTank.hasInventory() &&
            !(caa$observedTank.getInventory() instanceof TankAccessHandler));
        cir.setReturnValue(result);
    }

    @Inject(method = "updateConnectedInventory", at = @At("HEAD"))
    private void updateConnectedTank(CallbackInfo ci) {
        caa$observedTank.findNewCapability();
    }

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    private void getTankCapability(
        @NotNull Capability<?> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<?>> cir
    ) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            cir.setReturnValue(caa$tankCapability);
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
        caa$tankCapability.invalidate();
    }
}
