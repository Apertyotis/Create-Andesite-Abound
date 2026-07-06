package net.apertyotis.createandesiteabound.mixin.minecraft.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GameType.class)
public abstract class GameTypeMixin {
    // 任何模式玩家可飞行
    @WrapOperation(
        method = "updatePlayerAbilities",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1
        )
    )
    private void alwaysAllowFly(Abilities instance, boolean value, Operation<Void> original) {
        if (!AllConfig.always_allow_flying) {
            original.call(instance, value);
            return;
        }
        instance.mayfly = true;
    }
}
