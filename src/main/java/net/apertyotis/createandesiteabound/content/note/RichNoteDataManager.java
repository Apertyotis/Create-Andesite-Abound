package net.apertyotis.createandesiteabound.content.note;

import com.google.gson.Gson;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID)
public class RichNoteDataManager extends SimplePreparableReloadListener<Map<ResourceLocation, RichNoteData>> {

    public static final Gson GSON = new Gson();
    public static final RichNoteDataManager RELOAD_LISTENER = new RichNoteDataManager();

    public static RichNoteData cachedData;
    private static Map<ResourceLocation, RichNoteData> notes = Map.of();

    @SubscribeEvent
    public static void add(AddReloadListenerEvent event) {
        event.addListener(RELOAD_LISTENER);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected @NotNull Map<ResourceLocation, RichNoteData> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, RichNoteData> result = new HashMap<>();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("notes",
            key -> key.getNamespace().equals(CreateAndesiteAbound.MOD_ID) && key.getPath().endsWith(".json"));
        for (var entry: resources.entrySet()) {
            String path = entry.getKey().getPath();
            String id = path.substring("notes/".length(), path.length() - ".json".length());
            int split = id.indexOf('/');
            if (split < 0)
                continue;
            try {
                String space = id.substring(0, split);
                String name = id.substring(split + 1);
                ResourceLocation key = new ResourceLocation(space, name);
                try (Reader reader = entry.getValue().openAsReader()) {
                    RichNoteData data = GSON.fromJson(reader, RichNoteData.class);
                    if (data != null)
                        result.put(key, data);
                }
            } catch (ResourceLocationException | IOException ignored) {
            }
        }
        return result;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void apply(Map<ResourceLocation, RichNoteData> result, ResourceManager resourceManager, ProfilerFiller profiler) {
        notes = result;
    }

    public static RichNoteData get(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return notes.get(key);
    }
}
