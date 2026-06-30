package net.apertyotis.createandesiteabound.content.schematic.deploy;

import com.simibubi.create.infrastructure.config.AllConfigs;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.apertyotis.createandesiteabound.content.schematic.pack.StructureHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
    public @NotNull Component getName(@NotNull ItemStack stack) {
        // noinspection DataFlowIssue
        boolean temp = stack.hasTag() && stack.getTag().getBoolean("Temp");
        String name = temp ?
            Component.translatable("item.createandesiteabound.simple_schematic.temp").getString() :
            super.getName(stack).getString();
        String key = getTranslateKey(stack);
        if (key == null) {
            return Component.literal(name)
                .withStyle(ChatFormatting.LIGHT_PURPLE);
        } else {
            String trans = I18n.get(key);
            String deli = trans.startsWith("【") || trans.startsWith("「") ?
                "item.createandesiteabound.simple_schematic.dash.variant" :
                "item.createandesiteabound.simple_schematic.dash";
            return Component.literal(name)
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.translatable(deli)
                    .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(trans)
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        // noinspection DataFlowIssue
        if (stack.hasTag() && stack.getTag().getBoolean("Temp")) {
            int max = AllConfigs.server().schematics.maxSchematics.get();
            tooltip.add(Component.translatable("caa.schematic.limit", max).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, world, tooltip, flag);
    }

    public static String getTranslateKey(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("File")) {
                String fileName = tag.getString("File").strip();
                if (fileName.contains(".nbt")) {
                    String cleanName = fileName.replaceAll("§[0-9a-fk-or]", "");
                    return cleanName.endsWith(".nbt") ? cleanName.substring(0, cleanName.length() - 4) : cleanName;
                }
            }
        }

        return null;
    }

    public static StructureTemplate loadSchematic(Level world, ItemStack blueprint, String playerName) {
        StructureTemplate t = new StructureTemplate();
        CompoundTag tag = blueprint.getTag();
        if (tag == null)
            return null;
        String schematic = tag.getString("File");

        if (!schematic.endsWith(".nbt"))
            return null;

        Path dir;
        if (tag.getBoolean("Temp"))
            dir = world.isClientSide ?
                StructureHelper.getOrCreateClientTempSchematicPath() :
                StructureHelper.getOrCreateServerTempSchematicPath((ServerLevel) world).resolve(playerName);
        else
            dir = StructureHelper.getOrCreateSchematicPath();
        dir = dir.toAbsolutePath();
        Path file = Paths.get(schematic);

        Path path = dir.resolve(file).normalize();
        if (!path.startsWith(dir))
            return null;

        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {
            CompoundTag nbt = NbtIo.read(stream, new NbtAccounter(0x20000000L));
            t.load(world.holderLookup(Registries.BLOCK), nbt);
        } catch (IOException e) {
            CreateAndesiteAbound.LOGGER.warn("Failed to read schematic", e);
            return null;
        }

        return t;
    }
}
