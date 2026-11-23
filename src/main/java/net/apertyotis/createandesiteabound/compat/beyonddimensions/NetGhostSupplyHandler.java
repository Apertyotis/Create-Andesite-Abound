package net.apertyotis.createandesiteabound.compat.beyonddimensions;

import com.wintercogs.beyonddimensions.client.gui.DimensionsNetGUI;
import com.wintercogs.beyonddimensions.common.menu.DimensionsNetMenu;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.apertyotis.createandesiteabound.AllTags;
import net.apertyotis.createandesiteabound.compat.jei.GhostSupplyHandler;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class NetGhostSupplyHandler <T extends DimensionsNetGUI<?>> implements IGhostIngredientHandler<T> {
    @Override
    @ParametersAreNonnullByDefault
    public <I> @NotNull List<Target<I>> getTargetsTyped(T gui, ITypedIngredient<I> ingredient, boolean doStart) {
        ItemStack stack = ingredient.getItemStack().orElse(ItemStack.EMPTY);
        if (!AllTags.AllItemTags.GHOST_SUPPLY.matches(stack))
            return List.of();
        List<Target<I>> targets = new ArrayList<>();
        DimensionsNetMenu menu = gui.getMenu();
        int start = menu.inventoryStartIndex;
        int end = menu.inventoryEndIndex;
        for (int i = start; i < end; i++) {
            Slot slot = menu.getSlot(i);
            if (slot.isActive()) {
                targets.add(new GhostSupplyHandler.GhostSupplyTarget<>(
                    gui, slot, (i - start + 9) % 36, stack.getItem()));
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {}

    public static void register(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(DimensionsNetGUI.class, new NetGhostSupplyHandler<>());
    }
}
