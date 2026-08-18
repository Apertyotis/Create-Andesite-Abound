package net.apertyotis.createandesiteabound.mixin.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.library.gui.helpers.ScreenHelper;
import net.apertyotis.createandesiteabound.compat.jei.GhostFluidVesselHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;

@Mixin(value = ScreenHelper.class, remap = false)
public abstract class ScreenHelperMixin {
    @WrapOperation(
        method = "getGhostIngredientHandlers",
        at = @At(
            value = "NEW",
            target = "()Ljava/util/ArrayList;"
        )
    )
    private ArrayList<IGhostIngredientHandler<?>> forceRegisterGhostHandler(
        Operation<ArrayList<IGhostIngredientHandler<?>>> original,
        @Local(argsOnly = true) Screen guiScreen
    ) {
        ArrayList<IGhostIngredientHandler<?>> handlers = original.call();
        if (guiScreen instanceof InventoryScreen)
            handlers.add(new GhostFluidVesselHandler());
        return handlers;
    }
}
