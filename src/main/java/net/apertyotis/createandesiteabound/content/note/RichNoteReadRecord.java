package net.apertyotis.createandesiteabound.content.note;

import com.google.gson.*;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.apertyotis.createandesiteabound.foundation.PathHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID, value = Dist.CLIENT)
public class RichNoteReadRecord {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Map<ResourceLocation, String> Record = new HashMap<>();
    public static Path cachedPath;

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Record.clear();
        Path path = PathHelper.getRichNoteReadRecordPath();
        cachedPath = path;
        if (!Files.exists(path))
            return;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                try {
                    ResourceLocation id = new ResourceLocation(entry.getKey());
                    String hash = entry.getValue().getAsString();
                    Record.put(id, hash);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (Record.isEmpty())
            return;
        Path path = cachedPath;
        cachedPath = null;
        JsonObject json = new JsonObject();

        for (Map.Entry<ResourceLocation, String> entry : Record.entrySet())
            json.addProperty(entry.getKey().toString(), entry.getValue());
        Record.clear();

        try (Writer writer = Files.newBufferedWriter(
            path, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)
        ) {
            GSON.toJson(json, writer);
        } catch (IOException | JsonIOException ignored) {
        }
    }

    public static boolean isUnread(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return isUnread(key);
    }

    public static boolean isUnread(ResourceLocation key) {
        if (key == null)
            return true;
        String sha256 = Record.get(key);
        if (sha256 == null)
            return true;
        RichNoteData data = RichNoteDataManager.get(key);
        if (data == null)
            return true;
        return !sha256.equals(data.sha256);
    }

    public static void markRead(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        markRead(key);
    }

    public static void markRead(ResourceLocation key) {
        if (key == null)
            return;
        RichNoteData data = RichNoteDataManager.get(key);
        if (data == null)
            return;
        Record.put(key, data.sha256);
    }
}
