package net.apertyotis.createandesiteabound.mixin.create.contraption;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractContraptionEntity.class, remap = false)
public class AbstractContraptionEntityMixin {
    @Inject(method = "shouldActorTrigger", at = @At("HEAD"), cancellable = true)
    private void accuratePSI(
            MovementContext context, StructureTemplate.StructureBlockInfo blockInfo, MovementBehaviour actor,
            Vec3 actorPosition, BlockPos gridPosition, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!AllConfig.better_psi_on_carriage)
            return;

        if (actor instanceof PortableStorageInterfaceMovement &&
                context.contraption.entity instanceof CarriageContraptionEntity cce && cce.getCarriage() != null) {
            Train train = cce.getCarriage().train;
            boolean working = context.data.contains("WorkingPos");
            boolean arrived = train.getCurrentStation() != null;
            if (working ^ arrived) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}
