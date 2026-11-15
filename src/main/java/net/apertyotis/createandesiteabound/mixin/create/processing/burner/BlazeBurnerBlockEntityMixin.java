package net.apertyotis.createandesiteabound.mixin.create.processing.burner;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.apertyotis.createandesiteabound.AllConfigs;
import net.apertyotis.createandesiteabound.AllRecipeTypes;
import net.apertyotis.createandesiteabound.content.burner.LiquidBurningRecipe;
import net.apertyotis.createandesiteabound.content.burner.LiquidBurningRecipeWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class BlazeBurnerBlockEntityMixin extends SmartBlockEntity {

    @Shadow
    protected int remainingBurnTime;

    @Shadow
    protected FuelType activeFuel;

    @Shadow
    public abstract void updateBlockState();

    @Unique
    private SmartFluidTank caa$tank;

    @Unique
    private LazyOptional<IFluidHandler> caa$tankCapacity;

    @Unique
    private LiquidBurningRecipe caa$cachedRecipe;

    @Unique
    private boolean caa$firstTick = true;

    public BlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initCapability(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        caa$tank = new SmartFluidTank(1000, newFluid -> {
            if (newFluid.isEmpty()) {
                caa$cachedRecipe = null;
            } else if (caa$cachedRecipe == null || !caa$cachedRecipe.getFluidIngredient().test(newFluid)) {
                caa$cachedRecipe = caa$findRecipe(newFluid).orElse(null);
            }
        });
        caa$tank.setValidator(newFluid -> {
            if (newFluid.isEmpty())
                return false;
            FluidStack fluid = caa$tank.getFluid();
            if (!fluid.isEmpty())
                return true;
            return caa$findRecipe(newFluid).isPresent();
        });
        caa$tankCapacity = LazyOptional.of(() -> caa$tank);
    }

    @Unique
    private Optional<LiquidBurningRecipe> caa$findRecipe(FluidStack fluid) {
        if (level == null)
            return Optional.empty();
        LiquidBurningRecipeWrapper wrapper = new LiquidBurningRecipeWrapper(fluid);
        RecipeType<?> type = AllRecipeTypes.LIQUID_BURNING.type.get();
        // noinspection unchecked
        return level.getRecipeManager().getRecipeFor((RecipeType<LiquidBurningRecipe>) type, wrapper, level);
    }

    @Definition(id = "isCreative", field = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlockEntity;isCreative:Z")
    @Expression("?.isCreative")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean liquidBurningTick(boolean isCreative) {
        if (caa$firstTick) {
            caa$firstTick = false;
            caa$cachedRecipe = caa$findRecipe(caa$tank.getFluid()).orElse(null);
        }
        if (!isCreative) {
            caa$update();
        }
        return isCreative;
    }

    // 被动状态，消耗燃料开始燃烧
    // 黄火状态，可加时长或重置时长转超燃
    // 蓝火状态，可加时长
    @Unique
    private void caa$update() {
        if (caa$cachedRecipe == null) {
            if (AllConfigs.void_burning && activeFuel != FuelType.SPECIAL) {
                activeFuel = FuelType.NORMAL;
                remainingBurnTime = Math.max(remainingBurnTime, 200);
            }
            return;
        }

        FluidStack fluid = caa$tank.getFluid();
        if (caa$cachedRecipe.getFluidIngredient().getRequiredAmount() > fluid.getAmount())
            return;

        if (caa$cachedRecipe.isSuperheated()) {
            if (activeFuel != FuelType.SPECIAL) {
                remainingBurnTime = 0;
                activeFuel = FuelType.SPECIAL;
                updateBlockState();
            }
        } else if (activeFuel == FuelType.NONE) {
            remainingBurnTime = 0;
            activeFuel = FuelType.NORMAL;
            // updateBlockState();
        } else if (activeFuel == FuelType.SPECIAL){
            return;
        }

        if (remainingBurnTime < BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY / 2) {
            remainingBurnTime += caa$cachedRecipe.getBurnTime();
            caa$tank.drain(caa$cachedRecipe.getFluidIngredient().getRequiredAmount(), IFluidHandler.FluidAction.EXECUTE);
            notifyUpdate();
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return caa$tankCapacity.cast();
        }
        return super.getCapability(cap, side);
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void writeTank(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        compound.put("Tank", caa$tank.writeToNBT(new CompoundTag()));
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void readTank(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        caa$tank.readFromNBT(compound.getCompound("Tank"));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        caa$tankCapacity.invalidate();
    }
}
