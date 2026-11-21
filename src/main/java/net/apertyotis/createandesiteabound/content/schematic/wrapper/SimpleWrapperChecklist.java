package net.apertyotis.createandesiteabound.content.schematic.wrapper;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.StackRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.StrictNbtStackRequirement;
import net.apertyotis.createandesiteabound.mixin.create.schematics.SchematicPrinterAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("deprecation")
public class SimpleWrapperChecklist {
    private final Map<HashableRequirement, CheckEntry> itemMap = new HashMap<>();
    private final Map<HashableRequirement, CheckEntry> itemStackMap = new HashMap<>();

    private int completeItemEntries = 0;
    private int completeItemStackEntries = 0;

    private long currentAmount = 0;
    private long totalAmount = 0;

    public static SimpleWrapperChecklist create(Level world, ItemStack blueprint) {
        if (!AllItems.SCHEMATIC.isIn(blueprint))
            return null;

        SchematicPrinter printer = new SchematicPrinter();
        printer.loadSchematic(blueprint, world, true);
        if (!printer.isLoaded() || printer.isErrored() || printer.isWorldEmpty())
            return null;

        SchematicWorld reader = ((SchematicPrinterAccessor) printer).getBlockReader();
        SimpleWrapperChecklist checklist = new SimpleWrapperChecklist();
        for (BlockPos pos: reader.getAllPositions()) {
            BlockPos relPos = pos.offset(reader.anchor);
            BlockState required = reader.getBlockState(relPos);
            BlockEntity requiredBE = reader.getBlockEntity(relPos);

            ItemRequirement requirement = ItemRequirement.of(required, requiredBE);
            if (requirement.isEmpty() || requirement.isInvalid())
                continue;

            for (StackRequirement stackRequirement: requirement.getRequiredItems())
                checklist.putOrIncrement(stackRequirement);
        }

        reader.getEntityStream().forEach(entity -> {
            ItemRequirement requirement = ItemRequirement.of(entity);
            if (requirement.isEmpty() || requirement.isInvalid())
                return;
            for (StackRequirement stackRequirement: requirement.getRequiredItems())
                checklist.putOrIncrement(stackRequirement);
        });

        return checklist;
    }

    public void putOrIncrement(StackRequirement requirement) {
        if (requirement.stack.isEmpty())
            return;
        CheckEntry entry = (requirement instanceof StrictNbtStackRequirement ? itemStackMap : itemMap)
            .computeIfAbsent(new HashableRequirement(requirement), k -> new CheckEntry());
        entry.total += requirement.stack.getCount();
        totalAmount += requirement.stack.getCount();
    }

    public ItemStack countAndConsume(Player player, ItemStack stack) {
        ItemStack copy = stack.copy();
        if (damageItem(itemStackMap, copy, player))
            completeItemStackEntries++;

        if (consumeItem(itemStackMap, copy))
            completeItemStackEntries++;

        if (damageItem(itemMap, copy, player))
            completeItemEntries++;

        if (consumeItem(itemMap, copy))
            completeItemEntries++;

        return copy;
    }

    private boolean damageItem(Map<HashableRequirement, CheckEntry> map, ItemStack stack, Player player) {
        if (stack.isEmpty())
            return false;
        HashableRequirement key = new HashableRequirement(stack.getItem(), stack.getTag(), ItemUseType.DAMAGE);
        CheckEntry entry = map.get(key);
        if (entry == null || entry.isComplete())
            return false;
        do {
            int cost = Math.min(entry.total - entry.count, stack.getMaxDamage() - stack.getDamageValue());
            entry.count += cost;
            stack.hurtAndBreak(cost, player, p -> {});
            if (entry.isComplete())
                return true;
        } while (!stack.isEmpty());
        return false;
    }

    private boolean consumeItem(Map<HashableRequirement, CheckEntry> map, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        HashableRequirement key = new HashableRequirement(stack.getItem(), stack.getTag(), ItemUseType.CONSUME);
        CheckEntry entry = map.get(key);
        if (entry == null || entry.isComplete())
            return false;
        int cost = Math.min(entry.total - entry.count, stack.getCount());
        stack.shrink(cost);
        entry.count += cost;
        currentAmount += cost;
        return entry.isComplete();
    }

    public boolean isComplete() {
        return completeItemEntries >= itemMap.size() && completeItemStackEntries >= itemStackMap.size();
    }

    public float getProgress() {
        return totalAmount != 0 ? (float) currentAmount / totalAmount : 1;
    }

    public List<Map.Entry<ItemStack, CheckEntry>> getList() {
        List<Map.Entry<ItemStack, CheckEntry>> list1 = new ArrayList<>(), list2 = new ArrayList<>();
        for (var entry: itemStackMap.entrySet()) {
            ItemStack stack = entry.getKey().asStack();
            Map.Entry<ItemStack, CheckEntry> e = new AbstractMap.SimpleEntry<>(stack, entry.getValue());
            if (entry.getValue().isComplete())
                list1.add(e);
            else
                list2.add(e);
        }

        for (var entry: itemMap.entrySet()) {
            ItemStack stack = entry.getKey().asStack();
            Map.Entry<ItemStack, CheckEntry> e = new AbstractMap.SimpleEntry<>(stack, entry.getValue());
            if (entry.getValue().isComplete())
                list1.add(e);
            else
                list2.add(e);
        }
        list1.addAll(list2);
        return list1;
    }

    public ByteArrayTag serialize() {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(raw))) {
            out.writeInt(itemMap.size());
            for (var entry: itemMap.entrySet())
                writeEntry(entry, out);
            out.writeInt(itemStackMap.size());
            for (var entry: itemStackMap.entrySet())
                writeEntry(entry, out);
        } catch (IOException e) {
            return null;
        }
        return new ByteArrayTag(raw.toByteArray());
    }

    public static SimpleWrapperChecklist deserialize(ByteArrayTag bytes) {
        SimpleWrapperChecklist checklist = new SimpleWrapperChecklist();
        try (DataInputStream in = new DataInputStream(
            new GZIPInputStream(new ByteArrayInputStream(bytes.getAsByteArray())))
        ) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                var entry = readEntry(in);
                checklist.itemMap.put(entry.getKey(), entry.getValue());
                if (entry.getValue().isComplete())
                    checklist.completeItemEntries++;
                checklist.currentAmount += entry.getValue().count;
                checklist.totalAmount += entry.getValue().total;
            }
            count = in.readInt();
            for (int i = 0; i < count; i++) {
                var entry = readEntry(in);
                checklist.itemStackMap.put(entry.getKey(), entry.getValue());
                if (entry.getValue().isComplete())
                    checklist.completeItemStackEntries++;
                checklist.currentAmount += entry.getValue().count;
                checklist.totalAmount += entry.getValue().total;
            }
        } catch (IOException e) {
            return null;
        }
        return checklist;
    }

    public static SimpleWrapperChecklist deserialize(ItemStack stack) {
        if (!net.apertyotis.createandesiteabound.AllItems.SIMPLE_WRAPPER.isIn(stack))
            return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("File", Tag.TAG_STRING) || !tag.contains("Materials", Tag.TAG_BYTE_ARRAY))
            return null;
        return deserialize((ByteArrayTag) tag.get("Materials"));
    }

    private static void writeEntry(Map.Entry<HashableRequirement, CheckEntry> entry, DataOutput out) throws IOException {
        HashableRequirement key = entry.getKey();
        out.writeInt(BuiltInRegistries.ITEM.getId(key.item));
        if (key.nbt == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            NbtIo.write(key.nbt, out);
        }
        out.writeByte(key.type.ordinal());
        out.writeInt(entry.getValue().count);
        out.writeInt(entry.getValue().total);
    }

    private static Map.Entry<HashableRequirement, CheckEntry> readEntry(DataInput in) throws IOException {
        Item item = BuiltInRegistries.ITEM.byId(in.readInt());
        CompoundTag tag = in.readByte() == 1 ? NbtIo.read(in) : null;
        ItemUseType type = ItemUseType.values()[in.readByte()];
        HashableRequirement key = new HashableRequirement(item, tag, type);
        CheckEntry value = new CheckEntry();
        value.count = in.readInt();
        value.total = in.readInt();
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private static class HashableRequirement {

        private final Item item;
        private final CompoundTag nbt;
        private final ItemUseType type;

        private boolean initialized = false;
        private int hashCode;

        public HashableRequirement(StackRequirement requirement) {
            item = requirement.stack.getItem();
            type = requirement.usage;
            if (requirement instanceof StrictNbtStackRequirement && requirement.stack.hasTag())
                nbt = requirement.stack.getTag();
            else
                nbt = null;
        }

        public HashableRequirement(Item item, CompoundTag nbt, ItemUseType type) {
            this.item = item;
            this.nbt = nbt;
            this.type = type;
        }

        public ItemStack asStack() {
            return new ItemStack(item, 1, nbt);
        }

        @Override
        public int hashCode() {
            if (!initialized) {
                initialized = true;
                hashCode = item.hashCode();
                if (nbt != null) {
                    hashCode = hashCode * 31 + nbt.hashCode();
                }
                hashCode = (hashCode << 1) + type.ordinal();
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof HashableRequirement otherStack))
                return false;
            if (item != otherStack.item || type != otherStack.type)
                return false;
            if (nbt == null ^ otherStack.nbt == null)
                return false;
            return nbt == null || nbt.equals(otherStack.nbt);
        }
    }

    public static class CheckEntry {
        public int count;
        public int total;

        public boolean isComplete() {
            return count >= total;
        }
    }
}
