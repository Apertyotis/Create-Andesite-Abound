package net.apertyotis.createandesiteabound;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Optional;
import java.util.function.Supplier;

import static net.apertyotis.createandesiteabound.AllConfig.CLIENT;
import static net.apertyotis.createandesiteabound.AllConfig.SERVER;


@SuppressWarnings({"UnstableApiUsage", "SameParameterValue"})
public class ConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory client = builder.getOrCreateCategory(Component.translatable("caa.config.client.title"));
        client.addEntry(booleanEntry(entryBuilder, CLIENT.KEEP_FLYING_ON_GROUND, false));
        client.addEntry(intEntry(entryBuilder, CLIENT.TOOLBELT_HOLD_DELAY, false, 0, 100));
        client.addEntry(intEntry(entryBuilder, CLIENT.TOOLBELT_ANIMATION_TICKS, false, 0, 100));
        client.addEntry(booleanEntry(entryBuilder, CLIENT.QUICK_UNEQUIP_ITEMS, false));

        ConfigCategory qol = builder.getOrCreateCategory(Component.translatable("caa.config.qol.title"));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.BELT_FUNNEL_DETECTION_TWEAK,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.SPOUT_DOUBLE_CAPACITY,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.ALWAYS_ALLOW_FLYING,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.HEURISTIC_ROTATION,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.NO_DEPOT_OVERFLOW_DROP,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.REPLACE_ANY_FLOWING_FLUID,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.HARVESTER_NOT_CONSUME_SEED,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.DISABLE_DIG_SPEED_PENALTY,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.BETTER_PSI_ON_CARRIAGE,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.PLAYER_CAN_BREATH,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.HACHIMI_GLUE,true));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.PIPE_FLOW_COLLISION, true));
        qol.addEntry(intEntry(entryBuilder, SERVER.HOSE_PULLEY_SOUND_COOLDOWN, true, 0, Integer.MAX_VALUE));
        qol.addEntry(booleanEntry(entryBuilder, SERVER.EASY_BELT, true));

        ConfigCategory norm = builder.getOrCreateCategory(Component.translatable("caa.config.normalization.title"));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.DEPLOYER_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.CRUSHING_WHEEL_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.ITEM_DRAIN_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.MILLSTONE_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.MIXER_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.PRESS_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.SAW_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.SPOUT_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.CHUTE_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.PUMP_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.DEPOT_SPEED_CHANGE, true));
        norm.addEntry(booleanEntry(entryBuilder, SERVER.VALVE_SPEED_CHANGE, true));

        ConfigCategory misc = builder.getOrCreateCategory(Component.translatable("caa.config.misc.title"));
        misc.addEntry(intEntry(entryBuilder, SERVER.ITEM_ENTITY_LIFESPAN, true, 0, Integer.MAX_VALUE));
        misc.addEntry(booleanEntry(entryBuilder, SERVER.NONNULL_FLUID_VESSEL_STACKABLE, true));
        misc.addEntry(intEntry(entryBuilder, SERVER.FLUID_VESSEL_CAPACITY, true, 1, 256));
        misc.addEntry(intEntry(entryBuilder, SERVER.FLUID_MACHINE_CAPACITY, true, 1, 256));
        misc.addEntry(booleanEntry(entryBuilder, SERVER.DONT_COMPARE_ITEM_CAPABILITY, true));

        builder.setSavingRunnable(() -> {
            AllConfig.clientConfig.save();
            AllConfig.serverConfig.save();
            AllConfig.reloadClient();
            AllConfig.reloadServer();
        });
        return builder.build();
    }

    private static IntegerListEntry intEntry(
        ConfigEntryBuilder entryBuilder, ForgeConfigSpec.IntValue entry, boolean serverSide, int min, int max
    ) {
        String key = getI18NKey(entry);
        IntFieldBuilder builder = entryBuilder.startIntField(Component.translatable(key), entry.get())
            .setMin(min)
            .setMax(max)
            .setTooltipSupplier(tooltipFactory(key))
            .setDefaultValue(entry.getDefault())
            .setSaveConsumer(entry::set);
        if (serverSide)
            builder = builder.setRequirement(() -> Minecraft.getInstance().isLocalServer());
        return builder.build();
    }

    private static BooleanListEntry booleanEntry(
        ConfigEntryBuilder entryBuilder, ForgeConfigSpec.BooleanValue entry, boolean serverSide
    ) {
        String key = getI18NKey(entry);
        BooleanToggleBuilder builder = entryBuilder.startBooleanToggle(Component.translatable(key), entry.get())
            .setTooltipSupplier(tooltipFactory(key))
            .setDefaultValue(entry.getDefault())
            .setSaveConsumer(entry::set);
        if (serverSide)
            builder = builder.setRequirement(() -> Minecraft.getInstance().isLocalServer());
        return builder.build();
    }

    private static String getI18NKey(ForgeConfigSpec.ConfigValue<?> entry) {
        StringBuilder sb = new StringBuilder("caa.config");
        for (String path: entry.getPath()) {
            sb.append('.');
            sb.append(path);
        }
        return sb.toString();
    }

    private static Supplier<Optional<Component[]>> tooltipFactory(String key) {
        return () -> {
            int num = 0;
            while (num < 10) {
                if (!I18n.exists("%s.%d".formatted(key, num + 1)))
                    break;
                num++;
            }
            if (num == 0)
                return Optional.empty();
            Component[] tooltips = new Component[num];
            for (int i = 0; i < num; i++)
                tooltips[i] = Component.translatable("%s.%d".formatted(key, i + 1));
            return Optional.of(tooltips);
        };
    }
}
