package net.apertyotis.createandesiteabound.content.wrench.filtering;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.gui.ScreenOpener;
import mezz.jei.common.Internal;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.elements.IElement;
import net.apertyotis.createandesiteabound.AllPackets;
import net.apertyotis.createandesiteabound.foundation.RadialMenu;
import net.apertyotis.createandesiteabound.mixin.jei.BookmarkOverlayAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EasyFilteringHandlerClient {
    public static final EasyFilteringHandlerClient EASY_FILTERING_HANDLER_CLIENT = new EasyFilteringHandlerClient();

    public int heldTicks = -1;
    public BlockPos heldPos;
    public Direction heldSide;

    public boolean isInteracting(BlockPos pos) {
        return heldTicks != -1 && pos.equals(heldPos);
    }

    public void startInteraction(BlockPos pos, Direction side) {
        heldTicks = 0;
        heldPos = pos;
        heldSide = side;
    }

    public void reset() {
        heldTicks = -1;
        heldPos = null;
        heldSide = null;
    }

    public void tick() {
        if (heldTicks == -1)
            return;
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;
        if (level == null || player == null || player.isSpectator() || player.isShiftKeyDown() ||
            !AllItems.WRENCH.isIn(player.getMainHandItem()) || !mc.options.keyUse.isDown()) {
            reset();
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult) ||
            !blockHitResult.getBlockPos().equals(heldPos)) {
            reset();
            return;
        }
        FilteringBehaviour behaviour = BlockEntityBehaviour.get(mc.level, heldPos, FilteringBehaviour.TYPE);
        if (behaviour == null || !behaviour.testHit(blockHitResult.getLocation())) {
            reset();
            return;
        }

        if (heldTicks > 3)
            player.swinging = false;
        if (heldTicks++ < 5)
            return;
        if (Internal.getJeiRuntime().getBookmarkOverlay() instanceof BookmarkOverlay bookmark) {
            List<IElement<?>> elements = ((BookmarkOverlayAccessor) bookmark).getBookmarkList().getElements();
            List<ItemStack> filters = new ArrayList<>(8);
            for (int i = elements.size() - 1; i >= 0; i--) {
                ItemStack stack = elements.get(i).getTypedIngredient().getItemStack().orElse(ItemStack.EMPTY);
                if (!stack.isEmpty() && !(stack.getItem() instanceof FilterItem)) {
                    filters.add(stack.copy());
                }
            }
            if (!filters.isEmpty()) {
                BlockPos finalPos = heldPos;
                Direction finalSide = heldSide;
                ScreenOpener.open(new RadialMenu(filters, slot -> {
                    if (slot == -1)
                        return;
                    ItemStack stack = filters.get(slot);
                    AllPackets.getChannel().sendToServer(new EasyFilteringPacket(finalPos, finalSide, stack));
                }));
            }
        }
        reset();
    }
}
