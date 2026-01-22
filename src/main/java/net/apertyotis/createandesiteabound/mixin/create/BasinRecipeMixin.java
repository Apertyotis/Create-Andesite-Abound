package net.apertyotis.createandesiteabound.mixin.create;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.apertyotis.createandesiteabound.mixin.accessor.FilteringBehaviourAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasinRecipe.class, remap = false)
public abstract class BasinRecipeMixin {
    @Shadow
    private static boolean apply(BasinBlockEntity basin, Recipe<?> recipe, boolean test) {
        // 不会执行的方法体，仅为了通过编译器语法检查
        return false;
    }

    // 修复工作盆配方不正确匹配多产出配方过滤的问题
    @Inject(method = "match", at = @At("HEAD"), cancellable = true)
    private static void redirectFilterTest(BasinBlockEntity basin, Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        // 非机械动力加工配方不做修改
        if (recipe instanceof ProcessingRecipe<?> processingRecipe) {
            // 单产物配方不做修改
            if (processingRecipe.getRollableResults().size() + processingRecipe.getFluidResults().size() <= 1) {
                return;
            }

            // 获取过滤器
            FilteringBehaviour filter = basin.getFilter();
            if (filter == null) {
                cir.setReturnValue(false);
                return;
            }
            // 没有直接获得过滤器类的方法，使用accessor
            FilterItemStack filterItem = ((FilteringBehaviourAccessor) filter).getFilterItemStack();

            // 判断是否为黑名单模式，并判断是否为属性过滤器，因为属性过滤器无法识别流体
            boolean isWhitelist = true;
            boolean isAttributeFilter = false;
            if (filterItem instanceof FilterItemStack.ListFilterItemStack listFilter) {
                if (listFilter.isBlacklist)
                    isWhitelist = false;
            } else if (filterItem instanceof FilterItemStack.AttributeFilterItemStack attributeFilter) {
                isAttributeFilter = true;
                if (attributeFilter.whitelistMode == FilterItemStack.AttributeFilterItemStack.WhitelistMode.BLACKLIST)
                    isWhitelist = false;
            }

            for (ItemStack stack: processingRecipe.getRollableResultsAsItemStacks()) {
                boolean test = filter.test(stack);
                // 白名单下匹配任意可接受物品
                if (test && isWhitelist) {
                    cir.setReturnValue(apply(basin, recipe, true));
                    return;
                }
                // 黑名单下拒绝任意需拒绝物品
                if (!test && !isWhitelist) {
                    cir.setReturnValue(false);
                    return;
                }
            }

            // 属性过滤跳过流体检测
            if (!isAttributeFilter) {
                for (FluidStack stack: processingRecipe.getFluidResults()) {
                    boolean test = filter.test(stack);
                    // 同上
                    if (test && isWhitelist) {
                        cir.setReturnValue(apply(basin, recipe, true));
                        return;
                    }

                    if (!test && !isWhitelist) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }

            if (isWhitelist) {
                // 白名单下什么都没匹配到
                cir.setReturnValue(false);
            } else {
                // 黑名单匹配成功
                cir.setReturnValue(apply(basin, recipe, true));
            }
        }
    }
}
