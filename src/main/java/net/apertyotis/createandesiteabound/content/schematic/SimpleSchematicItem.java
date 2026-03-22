package net.apertyotis.createandesiteabound.content.schematic;

import com.simibubi.create.content.schematics.SchematicProcessor;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class SimpleSchematicItem extends Item {
    public SimpleSchematicItem(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("File")) {
                String fileName = tag.getString("File").strip();
                if (fileName.contains(".nbt")) {
                    String cleanName = fileName.replaceAll("§[0-9a-fk-or]", "");
                    String key = cleanName.endsWith(".nbt") ? cleanName.substring(0, cleanName.length() - 4) : cleanName;
                    MutableComponent mutableComponent = Component.translatable(key).withStyle(ChatFormatting.GOLD);
                    tooltip.add(mutableComponent);
                }
            }
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public static StructurePlaceSettings getSettings(ItemStack blueprint, boolean processNBT) {
        StructurePlaceSettings settings = new StructurePlaceSettings();
        CompoundTag tag = blueprint.getTag();
        if (tag != null) {
            String rotation = tag.getString("Rotation");
            String mirror = tag.getString("Mirror");

            for (Rotation i: Rotation.values()) {
                if (i.name().equals(rotation)) {
                    settings.setRotation(i);
                    break;
                }
            }

            for (Mirror i: Mirror.values()) {
                if (i.name().equals(mirror)) {
                    settings.setMirror(i);
                    break;
                }
            }
        }
        if (processNBT)
            settings.addProcessor(SchematicProcessor.INSTANCE);

        return settings;
    }

    public static StructureTemplate loadSchematic(HolderGetter<Block> lookup, ItemStack blueprint) {
        StructureTemplate t = new StructureTemplate();
        CompoundTag tag = blueprint.getTag();
        if (tag == null)
            return t;
        String schematic = tag.getString("File");

        if (!schematic.endsWith(".nbt"))
            return t;

        Path dir = Paths.get("schematics").toAbsolutePath();
        Path file = Paths.get(schematic);

        Path path = dir.resolve(file).normalize();
        if (!path.startsWith(dir))
            return t;

        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {
            CompoundTag nbt = NbtIo.read(stream, new NbtAccounter(0x20000000L));
            t.load(lookup, nbt);
        } catch (IOException e) {
            CreateAndesiteAbound.LOGGER.warn("Failed to read schematic", e);
        }

        return t;
    }
}
