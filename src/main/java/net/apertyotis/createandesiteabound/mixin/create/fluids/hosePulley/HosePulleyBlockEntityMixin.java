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
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
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

    @Inject(method = "sendData", at = @At("TAIL"))
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
        FluidStack fluid = handler.getFluidInTank(0);
        if (fluid.isEmpty()) {
            return false;
        } else if (!((FluidManipulationBehaviourAccessor) drainer).invokeCanDrainInfinitely(fluid.getFluid())) {
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
