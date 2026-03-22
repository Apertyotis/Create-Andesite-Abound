package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.simibubi.create.content.schematics.client.tools.RotateTool;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicHandler;

public class SimpleRotateTool extends RotateTool {
    @Override
    public void init() {
        super.init();
        schematicHandler = SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER;
    }
}
