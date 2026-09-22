package net.apertyotis.createandesiteabound.foundation;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TankAccessHandler implements IFluidHandler {
    Supplier<? extends IFluidHandler> capProvider;

    public TankAccessHandler(Supplier<? extends IFluidHandler> capProvider) {
        this.capProvider = capProvider;
    }

    private final ThreadLocal<Boolean> recursionGuard = ThreadLocal.withInitial(() -> false);

    private <T> T preventRecursion(Supplier<T> value, T defaultValue) {
        if (this.recursionGuard.get()) {
            return defaultValue;
        } else {
            this.recursionGuard.set(true);
            T result = value.get();
            this.recursionGuard.set(false);
            return result;
        }
    }

    @Override
    public int getTanks() {
        return preventRecursion(() -> {
            IFluidHandler handler = capProvider.get();
            return handler != null ? handler.getTanks() : 0;
        }, 0);
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return preventRecursion(() -> {
            IFluidHandler handler = capProvider.get();
            return handler != null ? handler.getFluidInTank(tank) : FluidStack.EMPTY;
        }, FluidStack.EMPTY);
    }

    @Override
    public int getTankCapacity(int tank) {
        return preventRecursion(() -> {
            IFluidHandler handler = capProvider.get();
            return handler != null ? handler.getTankCapacity(tank) : 0;
        }, 0);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return preventRecursion(() -> {
            IFluidHandler handler = capProvider.get();
            return handler != null && handler.isFluidValid(tank, stack);
        }, false);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return preventRecursion(() -> {
            IFluidHandler handler = capProvider.get();
            return handler != null ? handler.fill(resource, action) : 0;
        }, 0);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return preventRecursion(() -> {
            IFluidHandler handler = capProvider.get();
            return handler != null ? handler.drain(resource, action) : FluidStack.EMPTY;
        }, FluidStack.EMPTY);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return preventRecursion(() -> {
            IFluidHandler handler = capProvider.get();
            return handler != null ? handler.drain(maxDrain, action) : FluidStack.EMPTY;
        }, FluidStack.EMPTY);
    }
}
