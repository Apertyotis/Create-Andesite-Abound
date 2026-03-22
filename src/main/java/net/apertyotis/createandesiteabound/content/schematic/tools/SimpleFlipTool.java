package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.simibubi.create.content.schematics.client.tools.FlipTool;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicHandler;

public class SimpleFlipTool extends FlipTool {
    @Override
    public void init() {
        super.init();
        schematicHandler = SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER;
    }
}
