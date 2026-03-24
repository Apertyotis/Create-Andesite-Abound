package net.apertyotis.createandesiteabound.content.schematic;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;

public class SimpleSchematicPlacePacket extends SchematicPlacePacket {

    public SimpleSchematicPlacePacket(ItemStack stack) {
        super(stack);
    }

    public SimpleSchematicPlacePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public boolean handle(Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;

            Level world = player.level();
            SchematicPrinter printer = new SchematicPrinter();
            printer.loadSchematic(stack, world, !player.canUseGameMasterBlocks());
            if (!printer.isLoaded() || printer.isErrored())
                return;

            boolean includeAir = AllConfigs.server().schematics.creativePrintIncludesAir.get();

            while (printer.advanceCurrentPos()) {
                if (!printer.shouldPlaceCurrent(world))
                    continue;

                printer.handleCurrentTarget((pos, state, blockEntity) -> {
                    boolean placingAir = state.isAir();
                    if (placingAir && !includeAir)
                        return;

                    CompoundTag data = BlockHelper.prepareBlockEntityData(state, blockEntity);
                    BlockHelper.placeSchematicBlock(world, state, pos, null, data);
                }, (pos, entity) -> world.addFreshEntity(entity));
            }

            AllSoundEvents.SCHEMATICANNON_FINISH.playFrom(player);

            if (!player.isCreative()) {
                player.getMainHandItem().shrink(1);
            }
        });
        return true;
    }
}
