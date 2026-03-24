package net.apertyotis.createandesiteabound.mixin.create.trains;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.trains.entity.Carriage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;

@Mixin(value = Carriage.class, remap = false)
public abstract class CarriageMixin {

    /**
     * 修复火车错误保存乘客数据的问题<br>
     * @see net.minecraft.world.entity.Entity#saveAsPassenger(CompoundTag) 
     */
    @WrapOperation(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;"
            )
    )
    private Iterator<Entity> writePassengers(
            List<Entity> instance, Operation<Iterator<Entity>> original,
            @Local(name = "passengerMap") Map<Integer, CompoundTag> passengerMap,
            @Local(name = "mapping") Map<UUID, Integer> mapping
    ) {
        Iterator<Entity> it = original.call(instance);
        while (it.hasNext()) {
            Entity passenger = it.next();
            if (mapping.containsKey(passenger.getUUID())) {
                CompoundTag data = new CompoundTag();
                if (passenger.saveAsPassenger(data)) {
                    passengerMap.put(mapping.get(passenger.getUUID()), data);
                }
            }
        }
        // 返回空迭代器取消原循环
        return Collections.emptyIterator();
    }

    @WrapOperation(
            method = "serialize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;serializeNBT()Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private CompoundTag serializePassenger(Entity entity, Operation<CompoundTag> original) {
        CompoundTag data = new CompoundTag();
        entity.saveAsPassenger(data);
        return data;
    }
}
