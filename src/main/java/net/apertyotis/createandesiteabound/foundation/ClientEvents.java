package net.apertyotis.createandesiteabound.foundation;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;
import net.apertyotis.createandesiteabound.AllBlocks;
import net.apertyotis.createandesiteabound.CreateAndesiteAbound;
import net.apertyotis.createandesiteabound.content.note.RichNoteClientHandler;
import net.apertyotis.createandesiteabound.content.note.RichNoteTooltipComponent;
import net.apertyotis.createandesiteabound.content.wrench.filtering.EasyFilteringHandlerClient;
import net.apertyotis.createandesiteabound.content.hachimiGlue.HachimiGlueHandler;
import net.apertyotis.createandesiteabound.content.fluids.vessel.FluidVesselClickHandler;
import net.apertyotis.createandesiteabound.content.fluids.vessel.FluidVesselItem;
import net.apertyotis.createandesiteabound.content.radar.RedstoneRadarHandler;
import net.apertyotis.createandesiteabound.content.schematic.deploy.SimpleSchematicHandler;
import net.apertyotis.createandesiteabound.content.schematic.pack.SimplePackerHandler;
import net.apertyotis.createandesiteabound.content.toolbox.BetterToolboxHandlerClient;
import net.apertyotis.createandesiteabound.content.wrench.pickup.QuickPickupClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    public static int getKeyAttackCode() {
        InputConstants.Key keyAttack = Minecraft.getInstance().options.keyAttack.getKey();
        return keyAttack.getType() == InputConstants.Type.MOUSE ? keyAttack.getValue() : 0;
    }

    public static int getKeyUseCode() {
        InputConstants.Key keyUse = Minecraft.getInstance().options.keyUse.getKey();
        return keyUse.getType() == InputConstants.Type.MOUSE ? keyUse.getValue() : 0;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START)
            return;
        Level world = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (world == null || player == null)
            return;

        RedstoneRadarHandler.REDSTONE_RADAR_HANDLER.tick();
        SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER.tick();
        SimplePackerHandler.SIMPLE_PACKER_HANDLER.tick();
        HachimiGlueHandler.HACHIMI_GLUE_HANDLER.tick();
        EasyFilteringHandlerClient.EASY_FILTERING_HANDLER_CLIENT.tick();
        BetterToolboxHandlerClient.BETTER_TOOLBOX_HANDLER_CLIENT.tick();
        RichNoteClientHandler.tick();
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;

        int key = event.getKey();
        boolean pressed = event.getAction() != 0;

        SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER.onKeyInput(key, pressed);
    }

    @SubscribeEvent
    public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        double delta = event.getScrollDelta();

        if (SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER.mouseScrolled(delta) ||
            SimplePackerHandler.SIMPLE_PACKER_HANDLER.mouseScrolled(delta) ||
            HachimiGlueHandler.HACHIMI_GLUE_HANDLER.mouseScrolled(delta))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        double delta = event.getScrollDelta();

        if (RichNoteClientHandler.mouseScrolled(delta)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().screen != null)
            return;

        int button = event.getButton();
        boolean pressed = event.getAction() != 0;

        BetterToolboxHandlerClient.BETTER_TOOLBOX_HANDLER_CLIENT.onMouseInput(button, pressed);

        if (SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER.onMouseInput(button, pressed) ||
            SimplePackerHandler.SIMPLE_PACKER_HANDLER.onMouseInput(button, pressed) ||
            FluidVesselClickHandler.onMiddleClick(button, pressed) ||
            QuickPickupClientHandler.onMouseInput(button, pressed)
        ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLoadWorld(LevelEvent.Load event) {
        LevelAccessor world = event.getLevel();
        if (world.isClientSide() && world instanceof ClientLevel && !(world instanceof WrappedClientWorld)) {
            SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER.updateRenderers();
        }
    }

    @SubscribeEvent
    public static void onUnloadWorld(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide())
            return;
        SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER.updateRenderers();
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        PoseStack ms = event.getPoseStack();
        SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        ms.pushPose();

        SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER.render(ms, buffer, camera);

        buffer.draw();
        RenderSystem.enableCull();
        ms.popPose();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(ClientResourceReloadListener.RESOURCE_RELOAD_LISTENER);
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            // Register overlays in reverse order
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "simple_schematic", SimpleSchematicHandler.SIMPLE_SCHEMATIC_HANDLER);
        }

        @SubscribeEvent
        public static void registerClientTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(AssemblyContentTooltipComponent.class, Function.identity());
            event.register(RichNoteTooltipComponent.class, Function.identity());
        }

        @SubscribeEvent
        public static void registerFluidVesselDecorator(RegisterItemDecorationsEvent event) {
            event.register(AllBlocks.FLUID_VESSEL.asItem(), new FluidVesselItem.VesselItemDecorator());
        }
    }
}
