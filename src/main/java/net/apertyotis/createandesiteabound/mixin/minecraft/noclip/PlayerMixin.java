package net.apertyotis.createandesiteabound.mixin.minecraft.noclip;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apertyotis.createandesiteabound.content.trinklet.BottledGhost;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"
        )
    )
    private boolean noPhysics(Player player, Operation<Boolean> original) {
        return original.call(player) || BottledGhost.isFlyingNoclip(player);
    }

    @WrapOperation(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;touch(Lnet/minecraft/world/entity/Entity;)V"
        )
    )
    private void dontTouchExceptItem(Player player, Entity entity, Operation<Void> original) {
        if ((entity instanceof ItemEntity) || !BottledGhost.isFlyingNoclip(player))
            original.call(player, entity);
    }

    @WrapOperation(
        method = "updatePlayerPose",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"
        )
    )
    private boolean dontCrouch(Player player, Operation<Boolean> original) {
        return original.call(player) || BottledGhost.isFlyingNoclip(player);
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return BottledGhost.isFlyingNoclip((Player)(Object) this) ?
            PushReaction.IGNORE : super.getPistonPushReaction();
    }
}
