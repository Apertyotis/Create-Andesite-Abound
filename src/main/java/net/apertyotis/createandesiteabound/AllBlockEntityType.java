package net.apertyotis.createandesiteabound;

import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.apertyotis.createandesiteabound.content.fluids.emptying.EmptyingMachineBlockEntity;
import net.apertyotis.createandesiteabound.content.fluids.filling.FillingMachineBlockEntity;
import net.apertyotis.createandesiteabound.content.fluids.vessel.FluidVesselBlockEntity;
import net.apertyotis.createandesiteabound.content.radar.RedstoneRadarBlockEntity;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.REGISTRATE;

public class AllBlockEntityType {
    public static final BlockEntityEntry<RedstoneRadarBlockEntity> REDSTONE_RADAR = REGISTRATE
        .blockEntity("redstone_radar", RedstoneRadarBlockEntity::new)
        .validBlocks(AllBlocks.REDSTONE_RADAR)
        .register();

    public static final BlockEntityEntry<FluidVesselBlockEntity> FLUID_VESSEL = REGISTRATE
        .blockEntity("fluid_vessel", FluidVesselBlockEntity::new)
        .validBlocks(AllBlocks.FLUID_VESSEL)
        .register();

    public static final BlockEntityEntry<FillingMachineBlockEntity> FILLING_MACHINE = REGISTRATE
        .blockEntity("filling_machine", FillingMachineBlockEntity::new)
        .validBlocks(AllBlocks.FILLING_MACHINE)
        .renderer(() -> SmartBlockEntityRenderer::new)
        .register();

    public static final BlockEntityEntry<EmptyingMachineBlockEntity> EMPTYING_MACHINE = REGISTRATE
        .blockEntity("emptying_machine", EmptyingMachineBlockEntity::new)
        .validBlocks(AllBlocks.EMPTYING_MACHINE)
        .register();

    public static void register() {}
}
