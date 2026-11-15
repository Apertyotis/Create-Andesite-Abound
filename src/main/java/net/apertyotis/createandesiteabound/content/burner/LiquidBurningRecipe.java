package net.apertyotis.createandesiteabound.content.burner;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.apertyotis.createandesiteabound.AllRecipeTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LiquidBurningRecipe implements Recipe<LiquidBurningRecipeWrapper> {

    protected final ResourceLocation id;
    protected FluidIngredient fluidIngredient;
    protected int burnTime;
    protected boolean superheated;

    public LiquidBurningRecipe(ResourceLocation id, FluidIngredient fluidIngredient, int burnTime, boolean superheated) {
        this.id = id;
        this.fluidIngredient = fluidIngredient;
        this.burnTime = burnTime;
        this.superheated = superheated;
    }

    public FluidIngredient getFluidIngredient() {
        return fluidIngredient;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public boolean isSuperheated() {
        return superheated;
    }

    @Override
    public boolean matches(LiquidBurningRecipeWrapper wrapper, Level level) {
        return !wrapper.isEmpty() && fluidIngredient.test(wrapper.getFluid());
    }

    @Override
    public ItemStack assemble(LiquidBurningRecipeWrapper wrapper, RegistryAccess registry) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registry) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AllRecipeTypes.LIQUID_BURNING.serializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AllRecipeTypes.LIQUID_BURNING.type.get();
    }
}
