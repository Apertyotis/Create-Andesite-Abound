package net.apertyotis.createandesiteabound.mixin.createaddition;

import com.mrh0.createaddition.event.GameEvents;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL;

@Mixin(value = GameEvents.class, remap = false)
public abstract class GameEventsMixin {
    // 阻止对非创造玩家对创造烈焰人燃烧室使用吸管
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private static void ignoreCreativeBurner(PlayerInteractEvent.RightClickBlock evt, CallbackInfo ci) {
        BlockState state =  evt.getLevel().getBlockState(evt.getPos());
        BlockEntity be =  evt.getLevel().getBlockEntity(evt.getPos());
        if (!state.is(AllBlocks.BLAZE_BURNER.get()) || !(be instanceof BlazeBurnerBlockEntity burner)) {
            ci.cancel();
            return;
        }

        try {
            if (burner.isCreative() && !evt.getEntity().isCreative() &&
                    state.getValue(HEAT_LEVEL) != BlazeBurnerBlock.HeatLevel.SMOULDERING) {
                ci.cancel();
            }
        } catch (IllegalArgumentException ignored) {}

    }
}
