package net.apertyotis.createandesiteabound.mixin.create.logistics;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = FilterItemStack.class, remap = false)
public interface FilterItemStackAccessor {
    @Accessor("filterFluidStack")
    FluidStack getFilterFluidStack();
    @Invoker("resolveFluid")
    void invokeResolveFluid(Level level);
}
