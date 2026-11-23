package net.apertyotis.createandesiteabound.compat.sophisticatedbackpacks;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.apertyotis.createandesiteabound.AllTags;
import net.apertyotis.createandesiteabound.compat.jei.GhostSupplyHandler;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class BackpackGhostSupplyHandler<T extends StorageScreenBase<?>> implements IGhostIngredientHandler<T> {
    @Override
    @ParametersAreNonnullByDefault
    public <I> @NotNull List<Target<I>> getTargetsTyped(T gui, ITypedIngredient<I> ingredient, boolean doStart) {
        ItemStack stack = ingredient.getItemStack().orElse(ItemStack.EMPTY);
        if (!AllTags.AllItemTags.GHOST_SUPPLY.matches(stack))
            return List.of();
        List<Target<I>> targets = new ArrayList<>();
        StorageContainerMenuBase<?> menu = gui.getMenu();
        int start = menu.getNumberOfStorageInventorySlots();
        int end = Math.min(menu.realInventorySlots.size(), start + 36);
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
        registration.addGhostIngredientHandler(StorageScreenBase.class, new BackpackGhostSupplyHandler<>());
    }
}
