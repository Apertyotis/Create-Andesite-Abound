package net.apertyotis.createandesiteabound.content.schematic.pack;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimplePackerItem extends Item {

    public SimplePackerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        // noinspection DataFlowIssue
        return Component.translatable(stack.hasTag() && stack.getTag().getBoolean("Closure") ?
            "item.createandesiteabound.simple_packer.closure" : "item.createandesiteabound.simple_packer")
            .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new SimplePackerRenderer()));
    }
}
