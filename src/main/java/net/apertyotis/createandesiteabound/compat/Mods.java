package net.apertyotis.createandesiteabound.compat;

import net.minecraftforge.fml.ModList;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public enum Mods {
    CreateAddition;

    private final String id;

    Mods() {
        id = name().toLowerCase(Locale.ROOT);
    }

    public boolean isLoaded() {
        return ModList.get().isLoaded(id);
    }

    /**
     * Simple hook to run code if a mod is installed
     * @param toRun will be run only if the mod is loaded
     * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
     */
    public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
        if (isLoaded())
            return Optional.of(toRun.get().get());
        return Optional.empty();
    }
}
