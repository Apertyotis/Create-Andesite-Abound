package net.apertyotis.createandesiteabound.mixin.create.schematics;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.schematics.SchematicPrinter;
import net.apertyotis.createandesiteabound.AllItems;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicItem;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SchematicPrinter.class, remap = false)
public abstract class SchematicPrinterMixin {

    // 对简易蓝图重定向加载蓝图逻辑
    @WrapOperation(
            method = "loadSchematic",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/schematics/SchematicItem;loadSchematic(" +
                            "Lnet/minecraft/core/HolderGetter;Lnet/minecraft/world/item/ItemStack;)" +
                            "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;"
            )
    )
    private StructureTemplate redirectLoadSchematic(
            HolderGetter<Block> lookup, ItemStack blueprint, Operation<StructureTemplate> original
    ) {
        if (AllItems.SIMPLE_SCHEMATIC.isIn(blueprint))
            return SimpleSchematicItem.loadSchematic(lookup, blueprint);
        else
            return original.call(lookup, blueprint);
    }

    @WrapOperation(
            method = "loadSchematic",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/schematics/SchematicItem;getSettings(" +
                            "Lnet/minecraft/world/item/ItemStack;Z)" +
                            "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;"
            )
    )
    private StructurePlaceSettings redirectGetSettings(
            ItemStack blueprint, boolean processNBT, Operation<StructurePlaceSettings> original
    ) {
        if (AllItems.SIMPLE_SCHEMATIC.isIn(blueprint))
            return SimpleSchematicItem.getSettings(blueprint, processNBT);
        else
            return original.call(blueprint, processNBT);
    }
}
