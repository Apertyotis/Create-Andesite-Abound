package net.apertyotis.createandesiteabound.mixin.create.fluids.hosePulley;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;
import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.LangBuilder;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = HosePulleyBlockEntity.class, remap = false)
public abstract class HosePulleyBlockEntityMixin {

    @Shadow
    private FluidDrainingBehaviour drainer;
    @Shadow
    private FluidFillingBehaviour filler;
    @Shadow
    private HosePulleyFluidHandler handler;

    @Unique
    public boolean caa$fillerInfinite;
    @Unique
    public boolean caa$drainerInfinite;

    @Inject(method = "sendData", at = @At("HEAD"))
    private void sendInfinite(CallbackInfo ci) {
        caa$fillerInfinite = filler.isInfinite();
        caa$drainerInfinite = drainer.isInfinite();
    }

    // 向客户端发送更详细的infinite信息，以便于显示准确的护目镜提示
    @Inject(method = "write", at = @At("TAIL"))
    private void writeInfinite(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        if (clientPacket) {
            compound.putBoolean("FillerInfinite", caa$fillerInfinite);
            compound.putBoolean("DrainerInfinite", caa$drainerInfinite);
        }
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void readInfinite(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        if (clientPacket) {
            caa$fillerInfinite = compound.getBoolean("FillerInfinite");
            caa$drainerInfinite = compound.getBoolean("DrainerInfinite");
        }
    }

    @WrapOperation(
        method = "addToGoggleTooltip",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/fluids/hosePulley/HosePulleyBlockEntity;infinite:Z",
            opcode = Opcodes.GETFIELD
        )
    )
    private boolean addDetailedTooltip(
            HosePulleyBlockEntity instance, Operation<Boolean> original,
            @Local(argsOnly = true) List<Component> tooltip
    ) {
        if (!caa$drainerInfinite && !caa$fillerInfinite)
            return false;
        Level level = instance.getLevel();
        if (level == null)
            return false;
        // 软管滑轮流体能力实现较为特别，需要另外获取液体种类
        BlockPos pos = ((HosePulleyFluidHandlerAccessor) handler).getRootPosGetter().get();
        BlockState blockState = level.getBlockState(pos);
        Fluid fluid;
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            fluid = Fluids.WATER;
        } else if (blockState.getBlock() instanceof LiquidBlock liquidBlock) {
            fluid = liquidBlock.getFluid();
        } else {
            fluid = blockState.getFluidState().getType();
        }

        if (fluid == Fluids.EMPTY) {
            return false;
        } else if (!AllConfigs.server().fluids.bottomlessFluidMode.get().test(fluid)) {
            Component hint = Component.translatable("caa.hint.hose_pulley.cant_infinite")
                .withStyle(ChatFormatting.RED);
            for (Component line: TooltipHelper.cutTextComponent(hint, TooltipHelper.Palette.RED)) {
                new LangBuilder(CreateAndesiteAbound.MOD_ID).add(line).forGoggles(tooltip);
            }
            return false;
        } else if (caa$drainerInfinite) {
            return true;
        } else if (caa$fillerInfinite) {
            Component hint = Component.translatable("caa.hint.hose_pulley.lower_hose")
                .withStyle(ChatFormatting.GOLD);
            for (Component line: TooltipHelper.cutTextComponent(hint, TooltipHelper.Palette.YELLOW)) {
                new LangBuilder(CreateAndesiteAbound.MOD_ID).add(line).forGoggles(tooltip);
            }
            return false;
        }
        return original.call(instance);
    }
}
