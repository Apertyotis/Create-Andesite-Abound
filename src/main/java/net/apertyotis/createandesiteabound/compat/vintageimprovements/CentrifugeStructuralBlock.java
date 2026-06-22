package net.apertyotis.createandesiteabound.compat.vintageimprovements;

import net.minecraft.world.level.block.Block;
import com.negodya1.vintageimprovements.VintageBlocks;

public class CentrifugeStructuralBlock {
    public static boolean is(Block block) {
        return VintageBlocks.CENTRIFUGE_STRUCTURAL.is(block);
    }
}
