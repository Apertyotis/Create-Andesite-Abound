package net.apertyotis.createandesiteabound.foundation;

import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.MOD_ID;

public class PathHelper {

    public static Path getOrCreateSchematicPath() {
        Path path =  FMLPaths.CONFIGDIR.get().resolve(MOD_ID).resolve("structures");
        makeDirs(path);
        return path;
    }

    public static String getSaveNameOrServerIP() {
        Minecraft mc = Minecraft.getInstance();
        String save;
        if (mc.hasSingleplayerServer()) {
            // noinspection DataFlowIssue
            save = sanitize(mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString());
        } else {
            ServerData server = mc.getCurrentServer();
            save = server == null ? "unknown" : sanitize(server.ip);
        }
        return save;
    }

    public static Path getOrCreateServerTempSchematicPath(ServerLevel level) {
        Path path = level.getServer().getWorldPath(LevelResource.GENERATED_DIR).normalize()
            .resolve(MOD_ID).resolve("structures");
        makeDirs(path);
        return path;
    }

    public static Path getOrCreateClientTempSchematicPath() {
        String save = getSaveNameOrServerIP();
        Path path = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).resolve("temp").resolve(save);
        makeDirs(path);
        return path;
    }

    public static Path getRichNoteReadRecordPath() {
        String save = getSaveNameOrServerIP();
        Path path = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).resolve("notes");
        makeDirs(path);
        return path.resolve(save + ".json");
    }

    public static void makeDirs(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            CreateAndesiteAbound.LOGGER.warn("Could not create Folder: {}", path);
        }
    }

    public static String sanitize(String name) {
        return name.strip().replaceAll("[\\\\/:*?\"<>|]", "_").replace("..", "_");
    }
}
