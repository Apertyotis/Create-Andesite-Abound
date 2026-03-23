package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.simibubi.create.content.schematics.client.tools.*;

import java.util.List;

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

    public static List<ToolType> getTools() {
        return List.of(ToolType.MOVE, ToolType.MOVE_Y, ToolType.DEPLOY, ToolType.ROTATE, ToolType.PRINT);
    }

    public static SimpleToolType of(ToolType toolType) {
        return SimpleToolType.values()[toolType.ordinal()];
    }

    public ToolType getToolType() {
        return ToolType.values()[this.ordinal()];
    }
}
