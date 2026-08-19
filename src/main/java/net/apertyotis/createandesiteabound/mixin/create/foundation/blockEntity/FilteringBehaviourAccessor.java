package net.apertyotis.createandesiteabound.mixin.create.foundation.blockEntity;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FilteringBehaviour.class, remap = false)
public interface FilteringBehaviourAccessor {
    @Accessor("filter")
    FilterItemStack getFilterItemStack();
    @Accessor("filter")
    void setFilterInner(FilterItemStack filter);
}
