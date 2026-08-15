package net.apertyotis.createandesiteabound;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.apertyotis.createandesiteabound.content.liquid.vessel.FluidVesselBlock;
import net.apertyotis.createandesiteabound.content.liquid.vessel.FluidVesselItem;
import net.apertyotis.createandesiteabound.content.radar.RedstoneRadarBlock;
import net.apertyotis.createandesiteabound.content.radar.RedstoneRadarGenerator;
import net.apertyotis.createandesiteabound.content.radar.RedstoneRadarItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.REGISTRATE;


@SuppressWarnings("removal")
public class AllBlocks {
    public static final BlockEntry<RedstoneRadarBlock> REDSTONE_RADAR = REGISTRATE
        .block("redstone_radar", RedstoneRadarBlock::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())
        .properties(p -> p.isRedstoneConductor(($1, $2, $3) -> false))
        .properties(BlockBehaviour.Properties::noOcclusion)
        .addLayer(() -> RenderType::cutoutMipped)
        .blockstate(new RedstoneRadarGenerator()::generate)
        .item(RedstoneRadarItem::new)
        .transform(customItemModel())
        .register();

    public static final BlockEntry<FluidVesselBlock> FLUID_VESSEL = REGISTRATE
        .block("fluid_vessel", FluidVesselBlock::new)
        .properties(p -> p.sound((SoundType.NETHERITE_BLOCK)))
        .item(FluidVesselItem::new).build()
        .register();

    public static void register() {}
}
