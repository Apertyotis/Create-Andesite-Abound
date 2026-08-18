package net.apertyotis.createandesiteabound.compat.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.apertyotis.createandesiteabound.AllBlocks;
import net.apertyotis.createandesiteabound.AllPackets;
import net.apertyotis.createandesiteabound.content.fluids.vessel.GetFreeFluidVesselPacket;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class GhostFluidVesselHandler implements IGhostIngredientHandler<InventoryScreen> {

    @Override
    @ParametersAreNonnullByDefault
    public <I> @NotNull List<Target<I>> getTargetsTyped(InventoryScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        if (!AllBlocks.FLUID_VESSEL.isIn(ingredient.getItemStack().orElse(ItemStack.EMPTY)))
            return List.of();
        List<Target<I>> targets = new ArrayList<>();
        for (int i = 9; i < 45; i++) {
            Slot slot = gui.getMenu().getSlot(i);
            if (slot.isActive())
                targets.add(new GhostVesselTarget<>(gui, slot, i % 36));
        }
        return targets;
    }

    @Override
    public void onComplete() {}

    public static class GhostVesselTarget<I> implements Target<I> {
        final int index;
        final Rect2i area;

        public GhostVesselTarget(InventoryScreen gui, Slot slot, int index) {
            this.index = index;
            this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public @NotNull Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(@NotNull I ingredient) {
            AllPackets.getChannel().sendToServer(new GetFreeFluidVesselPacket(index));
        }
    }
}
