package net.apertyotis.createandesiteabound.mixin.minecraft.entity;

import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow(remap = false)
    public int lifespan;

    @Shadow
    @Nullable
    public abstract Entity getOwner();

    @Inject(method = "tick", at = @At("HEAD"))
    private void setLifespan(CallbackInfo ci) {
        if (lifespan == 6000 && lifespan != AllConfig.item_entity_lifespan && !(getOwner() instanceof Player))
            lifespan = AllConfig.item_entity_lifespan;
    }
}
