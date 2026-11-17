package net.apertyotis.createandesiteabound.compat.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.apertyotis.createandesiteabound.AllPackets;
import net.apertyotis.createandesiteabound.AllTags;
import net.apertyotis.createandesiteabound.foundation.GhostSupplyPacket;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class GhostSupplyHandler implements IGhostIngredientHandler<InventoryScreen> {

    @Override
    @ParametersAreNonnullByDefault
    public <I> @NotNull List<Target<I>> getTargetsTyped(InventoryScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        ItemStack stack = ingredient.getItemStack().orElse(ItemStack.EMPTY);
        if (!AllTags.AllItemTags.GHOST_SUPPLY.matches(stack))
            return List.of();
        List<Target<I>> targets = new ArrayList<>();
        for (int i = 9; i < 45; i++) {
            Slot slot = gui.getMenu().getSlot(i);
            if (slot.isActive())
                targets.add(new GhostSupplyTarget<>(gui, slot, i % 36, stack.getItem()));
        }
        return targets;
    }

    @Override
    public void onComplete() {}

    public static class GhostSupplyTarget<I> implements Target<I> {
        final int index;
        final Rect2i area;
        final Item item;

        public GhostSupplyTarget(InventoryScreen gui, Slot slot, int index, Item item) {
            this.index = index;
            this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
            this.item = item;
        }

        @Override
        public @NotNull Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(@NotNull I ingredient) {
            AllPackets.getChannel().sendToServer(new GhostSupplyPacket(index, item));
        }
    }
}
