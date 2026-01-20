package net.apertyotis.createandesiteabound.mixin.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = FluidRenderer.class, remap = false)
public abstract class FluidRendererMixin {
    // 修复蓝图打印注液器会崩溃的bug
    @WrapOperation(
            method = "renderFluidBox(Lnet/minecraftforge/fluids/FluidStack;FFFFFFLcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack;IZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private static Object preventRenderNullFluid(Function<ResourceLocation, TextureAtlasSprite> instance, Object t, Operation<?> original, @Cancellable CallbackInfo ci) {
        if (t == null) {
            ci.cancel();
            return null;
        }
        return original.call(instance, t);
    }
}
