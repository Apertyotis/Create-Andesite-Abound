package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.simibubi.create.content.schematics.client.tools.*;

public enum SimpleToolType {

    DEPLOY(new SimpleDeployTool()),
    MOVE(new SimpleMoveTool()),
    MOVE_Y(new SimpleMoveVerticalTool()),
    ROTATE(new SimpleRotateTool()),
    FLIP(new SimpleFlipTool()),
    PRINT(new SimplePlaceTool());

    private final ISchematicTool tool;

    SimpleToolType(ISchematicTool tool) {
        this.tool = tool;
    }

    public ISchematicTool getTool() {
        return tool;
    }

    public static SimpleToolType of(ToolType toolType) {
        return SimpleToolType.values()[toolType.ordinal()];
    }

    public ToolType getToolType() {
        return ToolType.values()[this.ordinal()];
    }
}
