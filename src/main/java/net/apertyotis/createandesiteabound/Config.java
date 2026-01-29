package net.apertyotis.createandesiteabound;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        // 构造配置
        Pair<Common, ForgeConfigSpec> pair = BUILDER.configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    // common 配置定义
    public static class Common {
        public final ForgeConfigSpec.BooleanValue DEPLOYER_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue CRUSHING_WHEEL_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue ITEM_DRAIN_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue MILLSTONE_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue MIXER_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue PRESS_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue SAW_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue SPOUT_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue CHUTE_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue PUMP_SPEED_CHANGE;

        public final ForgeConfigSpec.BooleanValue BELT_FUNNEL_DETECTION_TWEAK;
        public final ForgeConfigSpec.BooleanValue SPOUT_DOUBLE_CAPACITY;
        public final ForgeConfigSpec.BooleanValue NO_CHUTE_LEAKING;
        public final ForgeConfigSpec.BooleanValue ALWAYS_ALLOW_FLYING;

        Common(ForgeConfigSpec.Builder builder) {
            // 配方时间归一化
            builder.comment("Recipe Time Normalization").push("normalization");
            DEPLOYER_SPEED_CHANGE = builder
                    .comment("Set the Deployer’s full-speed processing time to 5 ticks.")
                    .define("deployer", true);
            CRUSHING_WHEEL_SPEED_CHANGE = builder
                    .comment("Set the Crushing Wheel’s full-speed processing time equals to recipe time and default value to 30 ticks.")
                    .define("crushing_wheel", true);
            ITEM_DRAIN_SPEED_CHANGE = builder
                    .comment("Set the Item Drain's rolling time to 10 ticks and recipe time to 10 ticks.")
                    .define("item_drain", true);
            MILLSTONE_SPEED_CHANGE = builder
                    .comment("Set the Millstone's full-speed processing time equals to recipe time.")
                    .define("millstone", true);
            MIXER_SPEED_CHANGE = builder
                    .comment("Set the Mechanical Mixer's default full-speed processing time to 15 ticks.")
                    .define("mixer", true);
            PRESS_SPEED_CHANGE = builder
                    .comment("Set the Mechanical Press's full-speed processing time to 10 ticks.")
                    .define("press", true);
            SAW_SPEED_CHANGE = builder
                    .comment("Set the Mechanical Saw's full-speed processing time to 10 ticks each bulk.")
                    .define("saw", true);
            SPOUT_SPEED_CHANGE = builder
                    .comment("Set the Spout's processing time to 20 ticks.")
                    .define("spout", true);
            CHUTE_SPEED_CHANGE = builder
                    .comment("Set the Chute's default transport time to 5 ticks.")
                    .define("chute", true);
            PUMP_SPEED_CHANGE = builder
                    .comment("Multiply the fluid network transfer speed by 8.")
                    .define("pump", true);
            builder.pop();

            // 其他非 bugfix 调整
            builder.comment("Behaviour Tweaks").push("tweaks");
            BELT_FUNNEL_DETECTION_TWEAK = builder
                    .comment("Allows funnels to extract items positioned exactly at the center of a belt.")
                    .comment("Previously, funnels could only extract items past the center.")
                    .comment("When a opposing funnel blocks items at the center, all side-facing belt funnels stop extracting, which is unintuitive.")
                    .define("belt_funnel_detection_tweak", true);
            SPOUT_DOUBLE_CAPACITY = builder
                    .comment("Set the spout's fluid capacity to 2000 mB.")
                    .define("spout_double_capacity", true);
            NO_CHUTE_LEAKING = builder
                    .comment("Prevent Diagonal Chutes from interacting with containers below.")
                    .comment("Because visually, Diagonal Chutes have no opening at the bottom.")
                    .define("no_chute_leaking", true);
            ALWAYS_ALLOW_FLYING = builder
                    .comment("Allow all players flying.")
                    .define("always_allow_flying",true);
            builder.pop();
        }
    }

    // 缓存配置值
    public static boolean deployer_speed_change;
    public static boolean crushing_wheel_speed_change;
    public static boolean item_drain_speed_change;
    public static boolean millstone_speed_change;
    public static boolean mixer_speed_change;
    public static boolean press_speed_change;
    public static boolean saw_speed_change;
    public static boolean spout_speed_change;
    public static boolean chute_speed_change;
    public static boolean pump_speed_change;

    public static boolean belt_funnel_detection_tweak;
    public static boolean spout_double_capacity;
    public static boolean no_chute_leaking;
    public static boolean always_allow_flying;

    // 重载配置时，更新缓存
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != COMMON_SPEC) return;

        deployer_speed_change = COMMON.DEPLOYER_SPEED_CHANGE.get();
        crushing_wheel_speed_change = COMMON.CRUSHING_WHEEL_SPEED_CHANGE.get();
        item_drain_speed_change = COMMON.ITEM_DRAIN_SPEED_CHANGE.get();
        millstone_speed_change = COMMON.MILLSTONE_SPEED_CHANGE.get();
        mixer_speed_change = COMMON.MIXER_SPEED_CHANGE.get();
        press_speed_change = COMMON.PRESS_SPEED_CHANGE.get();
        saw_speed_change = COMMON.SAW_SPEED_CHANGE.get();
        spout_speed_change = COMMON.SPOUT_SPEED_CHANGE.get();
        chute_speed_change = COMMON.CHUTE_SPEED_CHANGE.get();
        pump_speed_change = COMMON.PUMP_SPEED_CHANGE.get();

        belt_funnel_detection_tweak = COMMON.BELT_FUNNEL_DETECTION_TWEAK.get();
        spout_double_capacity = COMMON.SPOUT_DOUBLE_CAPACITY.get();
        no_chute_leaking = COMMON.NO_CHUTE_LEAKING.get();
        always_allow_flying = COMMON.ALWAYS_ALLOW_FLYING.get();
    }
}
