package net.apertyotis.createandesiteabound.mixin.create.equipment.toolbox;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.equipment.toolbox.RadialToolboxMenu;
import net.apertyotis.createandesiteabound.AllConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RadialToolboxMenu.class, remap = false)
public abstract class RadialToolboxMenuMixin {
    @Expression("? / 10.0")
    @ModifyExpressionValue(method = "renderWindow", at = @At("MIXINEXTRAS:EXPRESSION"))
    private float redirectOpenAnimationTicks(float original) {
        if (AllConfig.toolbelt_animation_ticks <= 0)
            return 1;
        return original * 10 / AllConfig.toolbelt_animation_ticks;
    }
}
