package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = BeltInventory.class, remap = false)
public interface BeltInventoryAccessor {
    @Accessor("beltMovementPositive")
    boolean isPositive();

    @Accessor("belt")
    BeltBlockEntity getBelt();

    @Invoker("insert")
    void invokeInsert(TransportedItemStack newStack);
}