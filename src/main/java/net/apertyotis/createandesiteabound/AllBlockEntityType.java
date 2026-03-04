package net.apertyotis.createandesiteabound;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.apertyotis.createandesiteabound.content.radar.RedstoneRadarBlockEntity;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.REGISTRATE;

public class AllBlockEntityType {
    public static final BlockEntityEntry<RedstoneRadarBlockEntity> REDSTONE_RADAR = REGISTRATE
            .blockEntity("redstone_radar", RedstoneRadarBlockEntity::new)
            .validBlocks(AllBlocks.REDSTONE_RADAR)
            .register();

    public static void register() {}
}
