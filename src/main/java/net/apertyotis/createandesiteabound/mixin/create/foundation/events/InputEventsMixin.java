package net.apertyotis.createandesiteabound.mixin.create.foundation.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxEquipPacket;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandler;
import com.simibubi.create.foundation.events.InputEvents;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.content.toolbox.BetterToolboxHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;
import java.util.List;

import static com.simibubi.create.AllItems.SCHEMATIC;
import static net.apertyotis.createandesiteabound.AllItems.SIMPLE_SCHEMATIC;

@Mixin(value = InputEvents.class, remap = false)
public abstract class InputEventsMixin {
    @WrapOperation(
        method = "onKeyInput",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/equipment/toolbox/ToolboxHandlerClient;onKeyInput(IZ)V"
        )
    )
    private static void wrapToolbeltKeyInput(int key, boolean pressed, Operation<Void> original) {
        if (key != AllKeys.TOOLBELT.getBoundCode())
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        if (AllKeys.TOOLBELT.getBoundCode() == AllKeys.TOOL_MENU.getBoundCode()) {
            ItemStack item = player.getMainHandItem();
            if (SCHEMATIC.isIn(item) || SIMPLE_SCHEMATIC.isIn(item))
                return;
        }

        BetterToolboxHandlerClient handler = BetterToolboxHandlerClient.BETTER_TOOLBOX_HANDLER_CLIENT;
        if (!pressed && AllConfig.quick_unequip_items && !handler.keyBlocked &&
            handler.keyPressed && handler.holdTicks <= AllConfig.toolbelt_hold_delay
        ) {
            List<ToolboxBlockEntity> toolboxes = ToolboxHandler.getNearest(player.level(), player, 8);
            toolboxes.sort(Comparator.comparing(ToolboxBlockEntity::getUniqueId));

            CompoundTag compound = player.getPersistentData().getCompound("CreateToolboxData");

            String slotKey = String.valueOf(player.getInventory().selected);
            boolean equipped = compound.contains(slotKey);

            if (equipped) {
                BlockPos pos = NbtUtils.readBlockPos(compound.getCompound(slotKey).getCompound("Pos"));
                double max = ToolboxHandler.getMaxRange(player);
                boolean canReachToolbox = ToolboxHandler.distance(player.position(), pos) < max * max;
                if (canReachToolbox) {
                    AllPackets.getChannel().sendToServer(new ToolboxEquipPacket(
                        pos, -5, player.getInventory().selected));
                }
            }
        }

        handler.keyPressed = pressed;
        if (!pressed) {
            handler.keyBlocked = false;
            handler.holdTicks = 0;
        }
    }
}
