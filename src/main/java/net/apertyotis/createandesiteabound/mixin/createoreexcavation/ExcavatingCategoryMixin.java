package net.apertyotis.createandesiteabound.mixin.createoreexcavation;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import com.tom.createores.jei.ExcavatingCategory;
import com.tom.createores.recipe.ExcavatingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ExcavatingCategory.class, remap = false)
public abstract class ExcavatingCategoryMixin {
    @Inject(
        method = "getTooltipStrings(Lcom/tom/createores/recipe/ExcavatingRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;DD)Ljava/util/List;",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
            shift = At.Shift.AFTER
        )
    )
    private void addStressImpactTooltip(
        ExcavatingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY,
        CallbackInfoReturnable<List<Component>> cir, @Local(name = "tooltip") List<Component> tooltip
    ) {
        LangBuilder rpmUnit = Lang.translate("generic.unit.rpm");
        tooltip.add(Lang.translate("tooltip.stressImpact")
            .style(ChatFormatting.GRAY)
            .component());

            int impact = recipe.getStress();
            IRotate.StressImpact impactId = IRotate.StressImpact.HIGH;
            LangBuilder builder = Lang.builder()
                .add(Lang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
                    .style(impactId.getAbsoluteColor()));
            tooltip.add(builder.add(Lang.number(impact))
                .text("x ")
                .add(rpmUnit)
                .component());
    }
}
