package net.apertyotis.createandesiteabound.mixin.create.fluids.hosePulley;

import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(value = HosePulleyFluidHandler.class, remap = false)
public interface HosePulleyFluidHandlerAccessor {
    @Accessor("rootPosGetter")
    Supplier<BlockPos> getRootPosGetter();
}
