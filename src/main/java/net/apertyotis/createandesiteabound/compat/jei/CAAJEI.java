package net.apertyotis.createandesiteabound.compat.jei;


import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.*;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.apertyotis.createandesiteabound.AllRecipeTypes;
import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.beyonddimensions.NetGhostSupplyHandler;
import net.apertyotis.createandesiteabound.compat.sophisticatedbackpacks.BackpackGhostSupplyHandler;
import net.apertyotis.createandesiteabound.content.burner.LiquidBurningRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.MOD_ID;

@SuppressWarnings({"removal", "unused"})
@JeiPlugin
public class CAAJEI implements IModPlugin {
    @SuppressWarnings("removal")
    private static final ResourceLocation ID = new ResourceLocation(MOD_ID, "jei_plugin");
    private static final List<CreateRecipeCategory<?>> ALL = new ArrayList<>();
    public IIngredientManager ingredientManager;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        ALL.clear();

        builder(LiquidBurningRecipe.class)
            .emptyBackground(177, 53)
            .itemIcon(AllBlocks.BLAZE_BURNER.get())
            .catalyst(AllBlocks.BLAZE_BURNER::get)
            .addTypedRecipes(AllRecipeTypes.LIQUID_BURNING::type)
            .build("liquid_burning", LiquidBurningCategory::new);

        builder(ConversionRecipe.class)
            .addRecipes(() -> SchematicPolishingCategory.RECIPES)
            .catalyst(AllItems.SCHEMATIC::get)
            .catalyst(net.apertyotis.createandesiteabound.AllItems.SIMPLE_WRAPPER::get)
            .emptyBackground(177, 55)
            .build("schematic_polishing", SchematicPolishingCategory::new);

        registration.addRecipeCategories(ALL.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ingredientManager = registration.getIngredientManager();
        ALL.forEach(c -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        ALL.forEach(c -> c.registerCatalysts(registration));
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        Mods.SophisticatedBackpacks.executeIfInstalled(
            () -> () -> BackpackGhostSupplyHandler.register(registration));
        Mods.BeyondDimensions.executeIfInstalled(
            () -> () -> NetGhostSupplyHandler.register(registration));
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Recipe<?>> CategoryBuilder<T> builder(Class<? extends T> clazz) {
        return new CategoryBuilder<>(clazz);
    }

    private static class CategoryBuilder<T extends Recipe<?>> {

        private final Class<? extends T> recipeClass;

        private IDrawable background;
        private IDrawable icon;

        private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
        private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

        public CategoryBuilder(Class<? extends T> recipeClass) {
            this.recipeClass = recipeClass;
        }

        public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<T>> consumer) {
            recipeListConsumers.add(consumer);
            return this;
        }

        public CategoryBuilder<T> addRecipes(Supplier<Collection<? extends T>> collection) {
            return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
        }

        public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipes::add, recipeType.get()));
        }

        public CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
            catalysts.add(supplier);
            return this;
        }

        public CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
            return catalystStack(() -> new ItemStack(supplier.get()
                .asItem()));
        }

        public CategoryBuilder<T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public CategoryBuilder<T> itemIcon(ItemLike item) {
            return icon(new ItemIcon(() -> new ItemStack(item)));
        }

        public CategoryBuilder<T> background(IDrawable background) {
            this.background = background;
            return this;
        }

        public CategoryBuilder<T> emptyBackground(int width, int height) {
            return background(new EmptyBackground(width, height));
        }

        public void build(String name, CreateRecipeCategory.Factory<T> factory) {
            Supplier<List<T>> recipesSupplier;
            recipesSupplier = () -> {
                List<T> recipes = new ArrayList<>();
                for (Consumer<List<T>> consumer : recipeListConsumers)
                    consumer.accept(recipes);
                return recipes;
            };

            CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
                new mezz.jei.api.recipe.RecipeType<>(new ResourceLocation(MOD_ID, name), recipeClass),
                Component.translatable("caa.recipe." + name), background, icon, recipesSupplier, catalysts);
            ALL.add(factory.create(info));
        }
    }
}