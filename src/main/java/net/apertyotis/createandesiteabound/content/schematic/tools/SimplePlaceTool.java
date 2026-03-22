package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.simibubi.create.content.schematics.client.tools.PlaceTool;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicHandler;

public class SimplePlaceTool extends PlaceTool {
    @Override
    public void init() {
        super.init();
        schematicHandler = SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER;
    }
}
