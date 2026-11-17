package net.apertyotis.createandesiteabound.mixin.suppementaries;

import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FaucetBlockTile.class, remap = false)
public interface FaucetBlockTileAccessor {
    @Accessor("transferCooldown")
    int getCooldown();

    @Accessor("transferCooldown")
    void setCooldown(int cooldown);
}
