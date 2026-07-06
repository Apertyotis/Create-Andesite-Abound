package net.apertyotis.createandesiteabound.mixin.create.kinetics.belt;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import net.apertyotis.createandesiteabound.content.belt.BeltBlockEntityEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = BeltConnectorItem.class, remap = false)
public abstract class BeltConnectorItemMixin {
    @Definition(id = "failed", local = @Local(type = boolean.class, name = "failed"))
    @Expression("failed")
    @ModifyExpressionValue(method = "createBelts", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean setTargetSpeed(
        boolean failed, @Local(argsOnly = true) Level world,
        @Local(name = "slope") BeltSlope slope, @Local(name = "facing") Direction facing,
        @Local(name = "beltsToCreate") List<BlockPos> beltsToCreate
    ) {
        if (!failed && (slope == BeltSlope.HORIZONTAL || slope == BeltSlope.UPWARD || slope == BeltSlope.DOWNWARD)) {
            int targetSpeed = facing == Direction.EAST || facing == Direction.NORTH ? -256 : 256;
            for (BlockPos pos: beltsToCreate) {
                if (world.getBlockEntity(pos) instanceof BeltBlockEntity belt) {
                    ((BeltBlockEntityEx) belt).caa$setTargetSpeed(targetSpeed);
                }
            }
        }
        return failed;
    }
}
