package net.apertyotis.createandesiteabound.content.schematic.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.schematics.client.tools.MoveVerticalTool;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicHandler;

public class SimpleMoveVerticalTool extends MoveVerticalTool {
    @Override
    public void init() {
        super.init();
        schematicHandler = SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER;
    }

    @Override
    public void renderOnSchematic(PoseStack ms, SuperRenderTypeBuffer buffer) {
        ISimpleSchematicTool.renderOnSchematic(ms, buffer, schematicHandler, renderSelectedFace, selectedFace);
    }
}
