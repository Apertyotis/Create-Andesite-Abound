package net.apertyotis.createandesiteabound;

import net.apertyotis.createandesiteabound.content.burner.LiquidBurningRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.MOD_ID;

@SuppressWarnings("removal")
public enum AllRecipeTypes {
    LIQUID_BURNING();

    public final ResourceLocation id;
    public final RegistryObject<RecipeSerializer<?>> serializer;
    public final RegistryObject<RecipeType<?>> type;

    AllRecipeTypes() {
        String name = name().toLowerCase(Locale.ROOT);
        id = new ResourceLocation(MOD_ID, name);
        type = Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(id));
        serializer = Registers.SERIALIZER_REGISTER.register(name, LiquidBurningRecipeSerializer::new);
    }

    public static void register(IEventBus modEventBus) {
        Registers.SERIALIZER_REGISTER.register(modEventBus);
        Registers.TYPE_REGISTER.register(modEventBus);
    }

    private static class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
        private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID);
    }
}
