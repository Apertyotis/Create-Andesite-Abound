package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.simibubi.create.content.schematics.client.tools.MoveVerticalTool;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicHandler;

public class SimpleMoveVerticalTool extends MoveVerticalTool {
    @Override
    public void init() {
        super.init();
        schematicHandler = SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER;
    }
}
