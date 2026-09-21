package net.apertyotis.createandesiteabound.mixin.create.schematics.client;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import net.apertyotis.createandesiteabound.foundation.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SchematicHandler.class, remap = false)
public abstract class SchematicHandlerMixin {
    @Expression("? != 1")
    @ModifyExpressionValue(method = "onMouseInput", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean rebindMouseKey(boolean ignored, @Local(argsOnly = true) int button) {
        return button != ClientEvents.getKeyUseCode();
    }
}
