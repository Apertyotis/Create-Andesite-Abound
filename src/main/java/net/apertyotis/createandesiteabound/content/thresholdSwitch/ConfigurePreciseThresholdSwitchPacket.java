package net.apertyotis.createandesiteabound.content.thresholdSwitch;

import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ConfigurePreciseThresholdSwitchPacket extends BlockEntityConfigurationPacket<ThresholdSwitchBlockEntity> {

    private int offBelow;
    private int onAbove;
    private boolean invert;
    private boolean inStacksOrBuckets;

    public ConfigurePreciseThresholdSwitchPacket(BlockPos pos, int offBelow, int onAbove, boolean invert, boolean inStacksOrBuckets) {
        super(pos);
        this.offBelow = offBelow;
        this.onAbove = onAbove;
        this.invert = invert;
        this.inStacksOrBuckets = inStacksOrBuckets;
    }

    public ConfigurePreciseThresholdSwitchPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        buffer.writeInt(offBelow);
        buffer.writeInt(onAbove);
        buffer.writeBoolean(invert);
        buffer.writeBoolean(inStacksOrBuckets);
    }

    @Override
    protected void readSettings(FriendlyByteBuf buffer) {
        offBelow = buffer.readInt();
        onAbove = buffer.readInt();
        invert = buffer.readBoolean();
        inStacksOrBuckets = buffer.readBoolean();
    }

    @Override
    protected void applySettings(ThresholdSwitchBlockEntity be) {
        ((ThresholdSwitchBlockEntityEx) be).caa$configure(offBelow, onAbove, invert, inStacksOrBuckets);
    }
}
