package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.simibubi.create.content.schematics.client.tools.DeployTool;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicHandler;

public class SimpleDeployTool extends DeployTool {
    @Override
    public void init() {
        super.init();
        schematicHandler = SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER;
    }
}
