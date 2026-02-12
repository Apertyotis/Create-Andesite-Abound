package net.apertyotis.createandesiteabound.mixin.create.processing.basin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(value = BasinOperatingBlockEntity.class, remap = false)
public interface BasinOperatingBlockEntityAccessor {
    @Invoker("getBasin")
    Optional<BasinBlockEntity> invokeGetBasin();
}
