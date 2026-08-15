package net.apertyotis.createandesiteabound;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.apertyotis.createandesiteabound.content.liquid.vessel.FluidVesselBlockEntity;
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

    public static void register() {}
}
