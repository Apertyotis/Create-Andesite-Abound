package net.apertyotis.createandesiteabound.mixin.create.foundation.blockEntity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import net.apertyotis.createandesiteabound.content.belt.BeltScrollValueBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ScrollValueRenderer.class, remap = false)
public abstract class ScrollValueRendererMixin {
    @Definition(id = "ctrlDown", method = "Lcom/simibubi/create/AllKeys;ctrlDown()Z")
    @Expression("ctrlDown()")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean ctrlDownForBelt(boolean original, @Local(name = "behaviour") ScrollValueBehaviour behaviour) {
        return original || behaviour instanceof BeltScrollValueBehaviour;
    }
}
