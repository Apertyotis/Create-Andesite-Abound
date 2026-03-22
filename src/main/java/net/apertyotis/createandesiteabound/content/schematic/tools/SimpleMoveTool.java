package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.simibubi.create.content.schematics.client.tools.MoveTool;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicHandler;

public class SimpleMoveTool extends MoveTool {
    @Override
    public void init() {
        super.init();
        schematicHandler = SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER;
    }
}
