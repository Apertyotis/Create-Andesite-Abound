package net.apertyotis.createandesiteabound.content.burner;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LiquidBurningRecipeSerializer implements RecipeSerializer<LiquidBurningRecipe> {
    @Override
    public @NotNull LiquidBurningRecipe fromJson(ResourceLocation id, JsonObject json) {
        int burnTime = GsonHelper.getAsInt(json, "burnTime");
        FluidIngredient fluid = FluidIngredient.deserialize(json.get("input"));
        boolean superheated = GsonHelper.getAsBoolean(json, "superheated", false);
        return new LiquidBurningRecipe(id, fluid, burnTime, superheated);
    }

    @Override
    public @Nullable LiquidBurningRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        int burnTime = buffer.readInt();
        boolean superheated = buffer.readBoolean();
        FluidIngredient fluid = FluidIngredient.read(buffer);
        return new LiquidBurningRecipe(id, fluid, burnTime, superheated);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, LiquidBurningRecipe recipe) {
        buffer.writeInt(recipe.burnTime);
        buffer.writeBoolean(recipe.superheated);
        recipe.fluidIngredient.write(buffer);
    }
}
