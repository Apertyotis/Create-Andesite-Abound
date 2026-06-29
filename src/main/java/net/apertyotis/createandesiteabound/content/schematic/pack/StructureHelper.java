package net.apertyotis.createandesiteabound.content.schematic.pack;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.utility.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.design_decor.LargeBoilerStructure;
import net.apertyotis.createandesiteabound.compat.vintageimprovements.CentrifugeStructuralBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class StructureHelper {

    public static boolean matchRotatedSize(Vec3i size, Vec3i size2, Rotation rotation) {
        return switch (rotation) {
            case NONE, CLOCKWISE_180 -> size.equals(size2);
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> size.getY() == size2.getY()
                && size.getX() == size2.getZ() && size.getZ() == size2.getX();
        };
    }

    public static boolean matchBlockInWorld(BlockInWorld inWorld, BlockState pattern) {
        BlockState state = inWorld.getState();
        // noinspection ConstantValue
        if (state == null || !pattern.is(state.getBlock()))
            return false;
        BlockEntity entity = inWorld.getEntity();
        return isIgnoredBlockEntity(entity) || matchPropertiesIgnoreRotation(state, pattern);
    }

    public static boolean matchRotatedFeature(
        Level world, BlockPos anchor, Vec3i length, Rotation rotation,
        List<Pair<BlockPos, Block>> feature, Long2ObjectMap<BlockInWorld> blockCache
    ) {
        for (var pair: feature) {
            BlockPos targetPos = transform(anchor, length, pair.getFirst(), rotation);
            BlockState state = blockCache.computeIfAbsent(targetPos.asLong(),
                k -> new BlockInWorld(world, targetPos, false)).getState();
            // noinspection ConstantValue
            if (state == null || !state.is(pair.getSecond()))
                return false;
        }
        return true;
    }

    public static boolean isIgnoredBlockEntity(BlockEntity entity) {
        return entity instanceof FluidTankBlockEntity;
    }

    public static final Set<Property<?>> ignoredProperties = Set.of(
        BlockStateProperties.FACING, BlockStateProperties.AXIS,
        BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HORIZONTAL_AXIS,
        BlockStateProperties.UP, BlockStateProperties.DOWN, BlockStateProperties.NORTH,
        BlockStateProperties.SOUTH, BlockStateProperties.WEST, BlockStateProperties.EAST
    );

    public static boolean matchPropertiesIgnoreRotation(BlockState state1, BlockState state2) {
        try {
            for (Property<?> property: state1.getProperties()) {
                if (ignoredProperties.contains(property))
                    continue;
                if (state1.getValue(property) != state2.getValue(property))
                    return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public static BlockPos transform(BlockPos anchor, Vec3i length, Vec3i pos, Rotation rotation) {
        return switch (rotation) {
            case NONE -> anchor.offset(pos);
            case CLOCKWISE_90 -> anchor.offset(length.getX() - pos.getZ(), pos.getY(), pos.getX());
            case CLOCKWISE_180 -> anchor.offset(length.getX() - pos.getX(), pos.getY(), length.getZ() - pos.getZ());
            case COUNTERCLOCKWISE_90 -> anchor.offset(pos.getZ(), pos.getY(), length.getZ() - pos.getX());
        };
    }

    public static BlockPos backToZero(Vec3i length, Rotation rotation) {
        return switch (rotation) {
            case NONE -> BlockPos.ZERO;
            case CLOCKWISE_90 -> new BlockPos(length.getX(), 0, 0);
            case CLOCKWISE_180 -> new BlockPos(length.getX(), 0, length.getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(0, 0, length.getZ());
        };
    }

    public static boolean loadTemplate(HolderLookup<Block> lookup, Path file, StructureTemplate template) {
        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
            new GZIPInputStream(Files.newInputStream(file, StandardOpenOption.READ)))))
        {
            CompoundTag nbt = NbtIo.read(stream, new NbtAccounter(0x20000000L));
            template.load(lookup, nbt);
            return true;
        } catch (IOException e) {
            CreateAndesiteAbound.LOGGER.warn("Failed to read schematic", e);
            return false;
        }
    }

    public static boolean shouldDestroyLater(Block block) {
        return AllBlocks.WATER_WHEEL_STRUCTURAL.is(block) || block instanceof LargeBoilerStructure ||
            Mods.VintageImprovements.runIfInstalled(() -> () -> CentrifugeStructuralBlock.is(block)).orElse(false);
    }
}
