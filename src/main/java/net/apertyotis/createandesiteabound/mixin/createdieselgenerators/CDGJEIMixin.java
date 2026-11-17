package net.apertyotis.createandesiteabound.mixin.createdieselgenerators;

import com.jesz.createdieselgenerators.compat.jei.CDGJEI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = CDGJEI.class, remap = false)
public abstract class CDGJEIMixin {
    @ModifyArg(
        method = "loadCategories",
        at = @At(
            value = "INVOKE",
            target = "Lcom/jesz/createdieselgenerators/compat/jei/CDGJEI$CategoryBuilder;emptyBackground(II)Lcom/jesz/createdieselgenerators/compat/jei/CDGJEI$CategoryBuilder;",
            ordinal = 1
        ),
        index = 1
    )
    private int resize(int height) {
        return height - 44;
    }
}
