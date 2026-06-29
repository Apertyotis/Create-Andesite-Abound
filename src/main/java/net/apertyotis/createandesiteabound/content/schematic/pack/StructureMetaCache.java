package net.apertyotis.createandesiteabound.content.schematic.pack;

import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.foundation.utility.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StructureMetaCache {
    private static final ExecutorService IO_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("AndesiteAbound-Schematics-Scan");
        thread.setDaemon(true);
        return thread;
    });

    private static final ConcurrentMap<Path, StructureMeta> metaCache = new ConcurrentHashMap<>();
    private static final AtomicLong timestamp = new AtomicLong();
    private static final int SCAN_MIN_INTERVAL = 20;

    public enum MatchResult {
        SUCCESS, SIZE_ERROR, BLOCK_ERROR
    }

    public static void matchAnyStructure(
        Level world, BlockPos anchor, Vec3i size,
        @NotNull BiConsumer<Path, SchematicWorld> success, Consumer<MatchResult> fail
    ) {
        if (timestamp.get() + SCAN_MIN_INTERVAL > world.getGameTime()) {
            matchInner(world, anchor, size, success, fail);
            return;
        }
        timestamp.set(world.getGameTime());

        HolderLookup<Block> lookup = world.holderLookup(Registries.BLOCK);
        CompletableFuture
            .runAsync(() -> updateAllCache(lookup), IO_EXEC)
            .thenAccept($ -> {
                if (world.getServer() == null)
                    return;
                world.getServer().execute(() -> {
                    metaCache.values().forEach(v -> v.calcFeature(world));
                    metaCache.values().removeIf(v -> v.feature == null);
                    matchInner(world, anchor, size, success, fail);
                });
            });
    }

    private static void matchInner(
        Level world, BlockPos anchor, Vec3i size,
        @NotNull BiConsumer<Path, SchematicWorld> success, Consumer<MatchResult> fail
    ) {
        Vec3i length = size.offset(-1, -1, -1);
        Long2ObjectMap<BlockInWorld> blockCache = new Long2ObjectOpenHashMap<>();
        boolean sizeMatched = false;
        for (var cacheEntry : metaCache.entrySet()) {
            for (Rotation rotation: Rotation.values()) {
                if (!StructureHelper.matchRotatedSize(size, cacheEntry.getValue().template.getSize(), rotation))
                    continue;
                sizeMatched = true;
                if (!StructureHelper.matchRotatedFeature(world, anchor, length, rotation, cacheEntry.getValue().feature, blockCache))
                    continue;

                SchematicWorld blockReader = new SchematicWorld(world);
                StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
                cacheEntry.getValue().template.placeInWorld(blockReader, StructureHelper.backToZero(length, rotation),
                    BlockPos.ZERO, settings, blockReader.getRandom(), Block.UPDATE_CLIENTS);
                boolean match = true;
                for (var blockEntry: blockReader.getBlockMap().entrySet()) {
                    BlockPos targetPos = anchor.offset(blockEntry.getKey());
                    BlockInWorld inWorld = blockCache.computeIfAbsent(targetPos.asLong(),
                        k -> new BlockInWorld(world, targetPos, false));
                    if (!StructureHelper.matchBlockInWorld(inWorld, blockEntry.getValue())) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    Path file = cacheEntry.getKey().subpath(1, cacheEntry.getKey().getNameCount());
                    success.accept(file, blockReader);
                    return;
                }
            }
        }
        if (fail != null) {
            fail.accept(sizeMatched ? MatchResult.BLOCK_ERROR : MatchResult.SIZE_ERROR);
        }
    }

    private static void updateAllCache(HolderLookup<Block> lookup) {
        Path root = Path.of("schematics");
        Path skip = root.resolve("uploaded");
        Set<Path> visited = new HashSet<>();
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>(){
                @ParametersAreNonnullByDefault
                @Override
                public @NotNull FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.equals(skip))
                        return FileVisitResult.SKIP_SUBTREE;
                    return FileVisitResult.CONTINUE;
                }

                @ParametersAreNonnullByDefault
                @Override
                public @NotNull FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().endsWith(".nbt"))
                        if (updateCache(lookup, file, attrs))
                            visited.add(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {}
        metaCache.keySet().retainAll(visited);
    }

    private static boolean updateCache(HolderLookup<Block> lookup, Path file, BasicFileAttributes attrs) {
        FileTime time = attrs.lastModifiedTime();
        StructureMeta meta = metaCache.get(file);
        if (meta != null && meta.timestamp.equals(time))
            return true;
        StructureTemplate template = new StructureTemplate();
        if (!StructureHelper.loadTemplate(lookup, file, template))
            return false;
        meta = StructureMeta.of(template, time);
        if (meta == null)
            return false;
        metaCache.put(file, meta);
        return true;
    }

    static class StructureMeta {
        FileTime timestamp;
        StructureTemplate template;
        List<Pair<BlockPos, Block>> feature;

        static StructureMeta of(StructureTemplate template, FileTime timestamp) {
            Vec3i size = template.getSize();
            if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0)
                return null;

            StructureMeta meta = new StructureMeta();
            meta.timestamp = timestamp;
            meta.template = template;

            return meta;
        }

        void calcFeature(Level world) {
            if (feature != null)
                return;
            SchematicWorld blockReader = new SchematicWorld(world);
            StructurePlaceSettings settings = new StructurePlaceSettings();
            template.placeInWorld(
                blockReader, BlockPos.ZERO, BlockPos.ZERO, settings, blockReader.getRandom(), Block.UPDATE_CLIENTS);
            if (blockReader.getBlockMap().isEmpty() && blockReader.getEntityStream().findAny().isEmpty())
                return;

            Map<Block, ArrayList<BlockPos>> invertedMap = new HashMap<>();
            for (var entry: blockReader.getBlockMap().entrySet()) {
                invertedMap.computeIfAbsent(entry.getValue().getBlock(), k -> new ArrayList<>())
                    .add(entry.getKey());
            }

            feature = blockReader.getBlockMap().entrySet().stream()
                .sorted(Comparator.comparingInt((e -> {
                    int depth = calcDepth(e.getKey(), template.getSize().offset(-1, -1, -1));
                    int amount = invertedMap.get(e.getValue().getBlock()).size();
                    return depth * invertedMap.size() + amount;
                })))
                .limit(8)
                .map(e -> Pair.of(e.getKey(), e.getValue().getBlock()))
                .toList();
        }

        static int calcDepth(Vec3i pos, Vec3i length) {
            Vec3i diff = length.subtract(pos);
            return Math.min(pos.getX(), diff.getX())
                + Math.min(pos.getY(), diff.getY())
                + Math.min(pos.getZ(), diff.getZ());
        }
    }
}