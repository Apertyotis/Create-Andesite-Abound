package net.apertyotis.createandesiteabound.content.schematic.wrapper;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.StackRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.StrictNbtStackRequirement;
import net.apertyotis.createandesiteabound.compat.Mods;
import net.apertyotis.createandesiteabound.compat.beyonddimensions.BeyondDimensions;
import net.apertyotis.createandesiteabound.compat.sophisticatedbackpacks.Backpacks;
import net.apertyotis.createandesiteabound.mixin.create.schematics.SchematicPrinterAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SimpleWrapperChecklist {
    public final Map<HashableRequirement, CheckEntry> itemMap = new HashMap<>();
    public final Map<HashableRequirement, CheckEntry> itemStackMap = new HashMap<>();
    public final Map<Fluid, CheckEntry> fluidMap = new HashMap<>();

    public int completeItemEntries = 0;
    public int completeItemStackEntries = 0;
    public int completeFluidEntries = 0;

    public long currentAmount = 0;
    public long totalAmount = 0;

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

            checklist.putOrIncrement(required.getFluidState());

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

    public void putOrIncrement(FluidState fluidState) {
        if (fluidState.isEmpty())
            return;
        Fluid fluid = fluidState.getType();
        if (fluid == Fluids.EMPTY || fluid == Fluids.FLOWING_WATER || fluid == Fluids.FLOWING_LAVA)
            return;
        if (fluid instanceof ForgeFlowingFluid forgeFluid) {
            if (!forgeFluid.isSource(fluidState))
                return;
            fluid = forgeFluid.getSource();
        }
        CheckEntry entry = fluidMap.computeIfAbsent(fluid, k -> new CheckEntry());
        entry.total++;
        totalAmount++;
    }

    public void gatherItems(Player player) {
        if (isComplete())
            return;
        IItemHandler handler = player.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP)
            .resolve().orElse(null);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            gatherItemsFrom(player, modifiable, stack -> {
                Optional<Boolean> isBackpack = Mods.SophisticatedBackpacks.runIfInstalled(
                    () -> () -> Backpacks.isBackpack(stack));
                return !isBackpack.orElse(false);
            }, items -> {
                Inventory inventory = player.getInventory();
                for (ItemStack stack: items) {
                    inventory.placeItemBackInInventory(stack);
                }
            });
        }

        if (!isComplete()) {
            Mods.SophisticatedBackpacks.executeIfInstalled(
                () -> () -> Backpacks.gatherMaterialsFromBackpacks(this, player));
        }
        if (!isComplete()) {
            Mods.BeyondDimensions.executeIfInstalled(
                () -> () -> BeyondDimensions.gatherMaterialsFromNetwork(this, player));
        }
    }

    public void gatherItemsFrom(
        Player player, IItemHandlerModifiable handler,
        Predicate<ItemStack> shouldDrain, Consumer<List<ItemStack>> placeBack
    ) {
        if (handler == null)
            return;
        List<ItemStack> overflowing = new ArrayList<>();
        for (int i = 0; i < handler.getSlots() && !isComplete(); i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (inSlot.isEmpty())
                continue;

            if (!isItemComplete()) {
                ConsumeResult result = countAndConsumeItem(player, inSlot);
                if (result.changed) {
                    if (!result.remaining.isEmpty()) {
                        handler.setStackInSlot(i, result.remaining);
                        overflowing.addAll(result.overflowing);
                    } else if (!result.overflowing.isEmpty()) {
                        handler.setStackInSlot(i, result.overflowing.get(0));
                        overflowing.addAll(result.overflowing.subList(1, result.overflowing.size()));
                    } else {
                        handler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }

            // 不处理不在背上的背包储罐能力
            if (!isFluidComplete() && shouldDrain.test(inSlot)) {
                ConsumeResult result = drainItem(inSlot);
                if (result.changed) {
                    if (!result.remaining.isEmpty()) {
                        handler.setStackInSlot(i, result.remaining);
                        overflowing.addAll(result.overflowing);
                    } else if (!result.overflowing.isEmpty()) {
                        handler.setStackInSlot(i, result.overflowing.get(0));
                        overflowing.addAll(result.overflowing.subList(1, result.overflowing.size()));
                    } else {
                        handler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }
        }
        placeBack.accept(overflowing);
    }

    public int drainFluidsFrom(IFluidHandler tank) {
        int drainedAmount = 0;
        if (tank != null) {
            for (int i = 0; i < tank.getTanks() && !isFluidComplete(); i++) {
                FluidStack inTank = tank.getFluidInTank(i);
                if (inTank.isEmpty() || inTank.getAmount() < FluidType.BUCKET_VOLUME)
                    continue;
                CheckEntry entry = fluidMap.get(inTank.getFluid());
                if (entry == null || entry.isComplete())
                    continue;
                int cost = Math.min(entry.total - entry.count, inTank.getAmount() / FluidType.BUCKET_VOLUME);
                FluidStack toDrain = inTank.copy();
                toDrain.setAmount(cost * FluidType.BUCKET_VOLUME);
                FluidStack drained = tank.drain(toDrain, IFluidHandler.FluidAction.SIMULATE);
                if (drained.getAmount() != cost * FluidType.BUCKET_VOLUME)
                    continue;
                tank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                drainedAmount += drained.getAmount();
                entry.count += cost;
                currentAmount += cost;
                if (entry.isComplete()) {
                    completeFluidEntries++;
                }
            }
        }
        return drainedAmount;
    }

    public ConsumeResult countAndConsumeItem(Player player, ItemStack stack) {
        ItemStack copy = stack.copy();
        List<ItemStack> overflowing = new ArrayList<>();
        ConsumeResult result = damageItem(true, copy, player);
        boolean changed = result.changed;
        if (result.changed) {
            copy = result.remaining;
            overflowing.addAll(result.overflowing);
        }
        changed |= consumeItem(true, copy);
        result = damageItem(false, copy, player);
        changed |= result.changed;
        if (result.changed) {
            copy = result.remaining;
            overflowing.addAll(result.overflowing);
        }
        changed |= consumeItem(false, copy);
        return new ConsumeResult(changed, copy, overflowing);
    }

    private ConsumeResult damageItem(boolean strictNbt, ItemStack stack, Player player) {
        if (stack.isEmpty())
            return ConsumeResult.EMPTY;
        HashableRequirement key = new HashableRequirement(stack.getItem(), strictNbt ? stack.getTag() : null, ItemUseType.DAMAGE);
        CheckEntry entry = (strictNbt ? itemStackMap : itemMap).get(key);
        if (entry == null || entry.isComplete())
            return ConsumeResult.EMPTY;
        boolean changed = false;
        List<ItemStack> overflowing = new ArrayList<>();
        // 考虑可堆叠的有耐久物品和空耐久不消失的物品
        do {
            int cost = Math.min(entry.total - entry.count, stack.getMaxDamage() - stack.getDamageValue());
            if (cost <= 0)
                break;
            ItemStack copy = stack.copyWithCount(1);
            stack.shrink(1);
            changed = true;
            entry.count += cost;
            currentAmount += cost;
            copy.hurtAndBreak(cost, player, p -> {});
            if (!copy.isEmpty())
                overflowing.add(copy);
            if (entry.isComplete()) {
                if (strictNbt)
                    completeItemStackEntries++;
                else
                    completeItemEntries++;
                break;
            }
        } while (!stack.isEmpty());
        return new ConsumeResult(changed, stack, overflowing);
    }

    private boolean consumeItem(boolean strictNbt, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        HashableRequirement key = new HashableRequirement(stack.getItem(), strictNbt ? stack.getTag() : null, ItemUseType.CONSUME);
        CheckEntry entry = (strictNbt ? itemStackMap : itemMap).get(key);
        if (entry == null || entry.isComplete())
            return false;
        int cost = Math.min(entry.total - entry.count, stack.getCount());
        stack.shrink(cost);
        entry.count += cost;
        currentAmount += cost;
        if (entry.isComplete()) {
            if (strictNbt)
                completeItemStackEntries++;
            else
                completeItemEntries++;
        }
        return true;
    }

    public ConsumeResult drainItem(ItemStack stack) {
        List<ItemStack> overflowing = new ArrayList<>();
        if (stack.isEmpty())
            return ConsumeResult.EMPTY;
        boolean changed = false;
        ItemStack copy = stack.copy();
        // 考虑可堆叠的液体能力物品和多储罐的液体能力物品
        while (true) {
            IFluidHandlerItem handler = copy.copyWithCount(1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .resolve().orElse(null);
            if (handler == null)
                return ConsumeResult.EMPTY;
            int drained = drainFluidsFrom(handler);
            if (drained > 0) {
                changed = true;
                copy.shrink(1);
                ItemStack leftover = handler.getContainer();
                if (!leftover.isEmpty())
                    overflowing.add(leftover);
            }
            if (copy.isEmpty() || drained <= 0)
                break;
        }
        return new ConsumeResult(changed, copy, overflowing);
    }

    public boolean isComplete() {
        return isItemComplete() && isFluidComplete();
    }

    public boolean isItemComplete() {
        return completeItemEntries >= itemMap.size() && completeItemStackEntries >= itemStackMap.size();
    }

    public boolean isFluidComplete() {
        return completeFluidEntries >= fluidMap.size();
    }

    public float getProgress() {
        return totalAmount != 0 ? (float) currentAmount / totalAmount : 1;
    }

    public List<Map.Entry<Either<ItemStack, Fluid>, CheckEntry>> getList() {
        List<Map.Entry<Either<ItemStack, Fluid>, CheckEntry>> list1 = new ArrayList<>(), list2 = new ArrayList<>();

        for (var entry: fluidMap.entrySet()) {
            var e = new AbstractMap.SimpleEntry<>(Either.<ItemStack, Fluid>right(entry.getKey()), entry.getValue());
            if (entry.getValue().isComplete())
                list2.add(e);
            else
                list1.add(e);
        }

        for (var entry: itemStackMap.entrySet()) {
            ItemStack stack = entry.getKey().asStack();
            var e = new AbstractMap.SimpleEntry<>(Either.<ItemStack, Fluid>left(stack), entry.getValue());
            if (entry.getValue().isComplete())
                list2.add(e);
            else
                list1.add(e);
        }

        for (var entry: itemMap.entrySet()) {
            ItemStack stack = entry.getKey().asStack();
            var e = new AbstractMap.SimpleEntry<>(Either.<ItemStack, Fluid>left(stack), entry.getValue());
            if (entry.getValue().isComplete())
                list2.add(e);
            else
                list1.add(e);
        }
        list1.addAll(list2);
        return list1;
    }

    public ByteArrayTag serialize() {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(raw))) {
            out.writeInt(itemMap.size());
            for (var entry: itemMap.entrySet())
                writeItemEntry(entry, out);
            out.writeInt(itemStackMap.size());
            for (var entry: itemStackMap.entrySet())
                writeItemEntry(entry, out);
            out.writeInt(fluidMap.size());
            for (var entry: fluidMap.entrySet())
                writeFluidEntry(entry, out);
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
                var entry = readItemEntry(in);
                checklist.itemMap.put(entry.getKey(), entry.getValue());
                if (entry.getValue().isComplete())
                    checklist.completeItemEntries++;
                checklist.currentAmount += entry.getValue().count;
                checklist.totalAmount += entry.getValue().total;
            }
            count = in.readInt();
            for (int i = 0; i < count; i++) {
                var entry = readItemEntry(in);
                checklist.itemStackMap.put(entry.getKey(), entry.getValue());
                if (entry.getValue().isComplete())
                    checklist.completeItemStackEntries++;
                checklist.currentAmount += entry.getValue().count;
                checklist.totalAmount += entry.getValue().total;
            }
            count = in.readInt();
            for (int i = 0; i < count; i++) {
                var entry = readFluidEntry(in);
                checklist.fluidMap.put(entry.getKey(), entry.getValue());
                if (entry.getValue().isComplete())
                    checklist.completeFluidEntries++;
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

    @SuppressWarnings("deprecation")
    private static void writeItemEntry(Map.Entry<HashableRequirement, CheckEntry> entry, DataOutput out) throws IOException {
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

    @SuppressWarnings("deprecation")
    private static Map.Entry<HashableRequirement, CheckEntry> readItemEntry(DataInput in) throws IOException {
        Item item = BuiltInRegistries.ITEM.byId(in.readInt());
        CompoundTag tag = in.readByte() == 1 ? NbtIo.read(in) : null;
        ItemUseType type = ItemUseType.values()[in.readByte()];
        HashableRequirement key = new HashableRequirement(item, tag, type);
        CheckEntry value = new CheckEntry();
        value.count = in.readInt();
        value.total = in.readInt();
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void writeFluidEntry(Map.Entry<Fluid, CheckEntry> entry, DataOutput out) throws IOException {
        Fluid key = entry.getKey();
        out.writeInt(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getID(key));
        out.writeInt(entry.getValue().count);
        out.writeInt(entry.getValue().total);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Map.Entry<Fluid, CheckEntry> readFluidEntry(DataInput in) throws IOException {
        Fluid key = ((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(in.readInt());
        CheckEntry value = new CheckEntry();
        value.count = in.readInt();
        value.total = in.readInt();
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static class HashableRequirement {

        private final Item item;
        private final CompoundTag nbt;
        private final ItemUseType type;

        private boolean initialized = false;
        private int hashCode;

        public HashableRequirement(StackRequirement requirement) {
            item = requirement.stack.getItem();
            type = requirement.usage;
            if (requirement instanceof StrictNbtStackRequirement)
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

    public record ConsumeResult(boolean changed, ItemStack remaining, List<ItemStack> overflowing) {
        static final ConsumeResult EMPTY = new ConsumeResult(false, ItemStack.EMPTY, List.of());
    }
}
