package net.apertyotis.createandesiteabound.content.radar;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

public class RedstoneRadarGenerator {
    public <T extends Block> void generate(DataGenContext<Block, T> ctx,
                                           RegistrateBlockstateProvider prov) {

        Block block = ctx.getEntry();
        String name = ctx.getName();

        ModelFile centerModel = prov.models()
                .withExistingParent(name + "_center",
                        prov.modLoc("block/redstone_radar/center"));

        ModelFile centerModel2 = prov.models()
                .withExistingParent(name + "_center2",
                        prov.modLoc("block/redstone_radar/center2"));

        ModelFile sideModel = prov.models()
                .withExistingParent(name + "_side",
                        prov.modLoc("block/redstone_radar/side"));

        MultiPartBlockStateBuilder builder =
                prov.getMultipartBuilder(block);

        builder.part()
                .modelFile(centerModel)
                .addModel()
                .condition(RedstoneRadarBlock.FORCELOAD, false)
                .end();

        builder.part()
                .modelFile(centerModel2)
                .addModel()
                .condition(RedstoneRadarBlock.FORCELOAD, true)
                .end();

        builder.part()
                .modelFile(sideModel)
                .rotationY(0)
                .addModel()
                .condition(BlockStateProperties.NORTH, true)
                .end();

        builder.part()
                .modelFile(sideModel)
                .rotationY(90)
                .addModel()
                .condition(BlockStateProperties.EAST, true)
                .end();

        builder.part()
                .modelFile(sideModel)
                .rotationY(180)
                .addModel()
                .condition(BlockStateProperties.SOUTH, true)
                .end();

        builder.part()
                .modelFile(sideModel)
                .rotationY(270)
                .addModel()
                .condition(BlockStateProperties.WEST, true)
                .end();
    }
}
