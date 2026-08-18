package net.apertyotis.createandesiteabound.content.fluids.vessel;

import net.apertyotis.createandesiteabound.AllBlocks;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID)
public class FluidVesselItemEntityHandler {
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item) {
            if (AllBlocks.FLUID_VESSEL.isIn(item.getItem()) && !item.getItem().hasTag()) {
                item.discard();
                event.setCanceled(true);
            }
        }
    }
}
