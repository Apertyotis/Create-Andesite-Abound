package net.apertyotis.createandesiteabound;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllConfig {
    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;
    public static ModConfig serverConfig;
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static ModConfig clientConfig;

    static {
        // 构造配置
        Pair<Server, ForgeConfigSpec> commonPair = (new ForgeConfigSpec.Builder()).configure(Server::new);
        SERVER = commonPair.getLeft();
        SERVER_SPEC = commonPair.getRight();

        Pair<Client, ForgeConfigSpec> clientPair = (new ForgeConfigSpec.Builder()).configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    // server 配置定义
    public static class Server {
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
        public final ForgeConfigSpec.BooleanValue DEPOT_SPEED_CHANGE;
        public final ForgeConfigSpec.BooleanValue VALVE_SPEED_CHANGE;

        public final ForgeConfigSpec.BooleanValue BELT_FUNNEL_DETECTION_TWEAK;
        public final ForgeConfigSpec.BooleanValue SPOUT_DOUBLE_CAPACITY;
        public final ForgeConfigSpec.BooleanValue ALWAYS_ALLOW_FLYING;
        public final ForgeConfigSpec.BooleanValue HEURISTIC_ROTATION;
        public final ForgeConfigSpec.BooleanValue NO_DEPOT_OVERFLOW_DROP;
        public final ForgeConfigSpec.BooleanValue REPLACE_ANY_FLOWING_FLUID;
        public final ForgeConfigSpec.BooleanValue HARVESTER_NOT_CONSUME_SEED;
        public final ForgeConfigSpec.BooleanValue DISABLE_DIG_SPEED_PENALTY;
        public final ForgeConfigSpec.BooleanValue DONT_COMPARE_ITEM_CAPABILITY;
        public final ForgeConfigSpec.BooleanValue BETTER_PSI_ON_CARRIAGE;
        public final ForgeConfigSpec.BooleanValue PLAYER_CAN_BREATH_UNDERWATER;
        public final ForgeConfigSpec.BooleanValue HACHIMI_GLUE;
        public final ForgeConfigSpec.BooleanValue PIPE_FLOW_COLLISION;
        public final ForgeConfigSpec.IntValue HOSE_PULLEY_SOUND_COOLDOWN;
        public final ForgeConfigSpec.IntValue ITEM_ENTITY_LIFESPAN;
        public final ForgeConfigSpec.IntValue FLUID_VESSEL_CAPACITY;
        public final ForgeConfigSpec.IntValue FLUID_MACHINE_CAPACITY;

        Server(ForgeConfigSpec.Builder builder) {
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
                .comment("Increase fluid flow propagation speed to 16 blocks/tick.")
                .define("pump", true);
            DEPOT_SPEED_CHANGE = builder
                .comment("Set the Depot item movement animation duration to 5 ticks.")
                .define("depot", true);
            VALVE_SPEED_CHANGE = builder
                .comment("Set the Valve state toggle duration at full speed to 1 tick.")
                .define("valve", true);
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
            ALWAYS_ALLOW_FLYING = builder
                .comment("Allow all players flying.")
                .define("always_allow_flying",true);
            HEURISTIC_ROTATION = builder
                .comment("Allow rotation for blocks without a custom rotate implementation.")
                .comment("Many mod authors do not override rotate and mirror when adding directional blocks.")
                .comment("As a result, vanilla structure block placement and Create schematic printing are unable to correctly rotate these blocks.")
                .comment("Enabling this feature automatically detects common facing properties and applies rotation/mirror.")
                .define("heuristic_rotation", true);
            NO_DEPOT_OVERFLOW_DROP = builder
                .comment("Prevent the depot from dropping overflow items.")
                .comment("For example when it accumulates too many processing outputs.")
                .comment("Or when an Ejector cannot merge received item stacks.")
                .define("no_depot_overflow_drop", true);
            REPLACE_ANY_FLOWING_FLUID = builder
                .comment("Allow pumps to replace any flowing fluid, ignoring the fluid type.")
                .define("replace_any_flowing_fluid", true);
            HARVESTER_NOT_CONSUME_SEED = builder
                .comment("Prevent harvester from consuming seed when replanting.")
                .define("harvester_not_consume_seed", true);
            DISABLE_DIG_SPEED_PENALTY = builder
                .comment("When players are in the air or water, they will no longer be subject to a five-time increase in digging time penalty")
                .define("disable_dig_speed_penalty", true);
            DONT_COMPARE_ITEM_CAPABILITY = builder
                .comment("Prevent forge initializing the item capabilities when comparing item stacks.")
                .define("dont_compare_item_capability", true);
            BETTER_PSI_ON_CARRIAGE = builder
                .comment("PSIs on trains are now only activated when the train arrives at a station.")
                .define("better_psi_on_carriage", true);
            PLAYER_CAN_BREATH_UNDERWATER = builder
                .comment("Player won't drown underwater.")
                .define("player_can_breath_underwater", true);
            HACHIMI_GLUE = builder
                .comment("Make super glue as convenient as honey glue from Aeronautic.")
                .define("hachimi_glue", true);
            ITEM_ENTITY_LIFESPAN = builder
                .comment("Override the lifespan of Item Entities created from non-player drops.")
                .defineInRange("item_entity_lifespan", 1200, 0, Integer.MAX_VALUE);
            PIPE_FLOW_COLLISION = builder
                .define("pipe_flow_collision", true);
            HOSE_PULLEY_SOUND_COOLDOWN = builder
                .defineInRange("hose_pulley_sound_cooldown", 10, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.comment("Fluid Filling&Emptying").push("fluid");
            FLUID_VESSEL_CAPACITY = builder
                .defineInRange("fluid_vessel_capacity", 16, 1, 256);
            FLUID_MACHINE_CAPACITY = builder
                .defineInRange("fluid_machine_capacity", 9, 1, 256);
            builder.pop();
        }
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue KEEP_FLYING_ON_GROUND;
        public final ForgeConfigSpec.IntValue TOOLBELT_HOLD_DELAY;
        public final ForgeConfigSpec.IntValue TOOLBELT_ANIMATION_TICKS;
        public final ForgeConfigSpec.BooleanValue QUICK_UNEQUIP_ITEMS;

        Client(ForgeConfigSpec.Builder builder) {
            KEEP_FLYING_ON_GROUND = builder
                .comment("Prevents players from automatically exiting flight mode when touching the ground.")
                .define("keep_flying_on_ground", true);
            TOOLBELT_HOLD_DELAY = builder
                .comment("Ticks the toolbelt hotkey must be held before activating.")
                .comment("By the way, this key is blocked while holding a schematic.")
                .defineInRange("toolbelt_hold_delay", 3, 0, 100);
            TOOLBELT_ANIMATION_TICKS = builder
                .comment("Duration of the Toolbelt opening animation.")
                .comment("Set to 0 to open instantly. (Vanilla: 10)")
                .defineInRange("toolbelt_animation_ticks", 2, 0, 100);
            QUICK_UNEQUIP_ITEMS = builder
                .comment("Short-press the toolbelt hotkey to quickly unequip items without opening the toolbox.")
                .define("quick_unequip_items", true);
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
    public static boolean depot_speed_change;
    public static boolean valve_speed_change;

    public static boolean belt_funnel_detection_tweak;
    public static boolean spout_double_capacity;
    public static boolean always_allow_flying;
    public static boolean heuristic_rotation;
    public static boolean no_depot_overflow_drop;
    public static boolean replace_any_flowing_fluid;
    public static boolean harvester_not_consume_seed;
    public static boolean disable_dig_speed_penalty;
    public static boolean dont_compare_item_capability;
    public static boolean better_psi_on_carriage;
    public static boolean player_can_breath_underwater;
    public static boolean keep_flying_on_ground;
    public static boolean hachimi_glue;
    public static boolean pipe_flow_collision;
    public static int item_entity_lifespan;
    public static int fluid_vessel_capacity;
    public static int fluid_machine_capacity;
    public static int toolbelt_hold_delay;
    public static int toolbelt_animation_ticks;
    public static boolean quick_unequip_items;
    public static int hose_pulley_sound_cooldown;

    // 重载配置时，更新缓存
    public static void reloadServer() {
        deployer_speed_change = SERVER.DEPLOYER_SPEED_CHANGE.get();
        crushing_wheel_speed_change = SERVER.CRUSHING_WHEEL_SPEED_CHANGE.get();
        item_drain_speed_change = SERVER.ITEM_DRAIN_SPEED_CHANGE.get();
        millstone_speed_change = SERVER.MILLSTONE_SPEED_CHANGE.get();
        mixer_speed_change = SERVER.MIXER_SPEED_CHANGE.get();
        press_speed_change = SERVER.PRESS_SPEED_CHANGE.get();
        saw_speed_change = SERVER.SAW_SPEED_CHANGE.get();
        spout_speed_change = SERVER.SPOUT_SPEED_CHANGE.get();
        chute_speed_change = SERVER.CHUTE_SPEED_CHANGE.get();
        pump_speed_change = SERVER.PUMP_SPEED_CHANGE.get();
        depot_speed_change = SERVER.DEPOT_SPEED_CHANGE.get();
        valve_speed_change = SERVER.VALVE_SPEED_CHANGE.get();

        belt_funnel_detection_tweak = SERVER.BELT_FUNNEL_DETECTION_TWEAK.get();
        spout_double_capacity = SERVER.SPOUT_DOUBLE_CAPACITY.get();
        always_allow_flying = SERVER.ALWAYS_ALLOW_FLYING.get();
        heuristic_rotation = SERVER.HEURISTIC_ROTATION.get();
        no_depot_overflow_drop = SERVER.NO_DEPOT_OVERFLOW_DROP.get();
        replace_any_flowing_fluid = SERVER.REPLACE_ANY_FLOWING_FLUID.get();
        harvester_not_consume_seed = SERVER.HARVESTER_NOT_CONSUME_SEED.get();
        disable_dig_speed_penalty = SERVER.DISABLE_DIG_SPEED_PENALTY.get();
        dont_compare_item_capability = SERVER.DONT_COMPARE_ITEM_CAPABILITY.get();
        better_psi_on_carriage = SERVER.BETTER_PSI_ON_CARRIAGE.get();
        player_can_breath_underwater = SERVER.PLAYER_CAN_BREATH_UNDERWATER.get();
        hachimi_glue = SERVER.HACHIMI_GLUE.get();
        pipe_flow_collision = SERVER.PIPE_FLOW_COLLISION.get();
        item_entity_lifespan = SERVER.ITEM_ENTITY_LIFESPAN.get();
        fluid_vessel_capacity = SERVER.FLUID_VESSEL_CAPACITY.get();
        fluid_machine_capacity = SERVER.FLUID_MACHINE_CAPACITY.get();
        hose_pulley_sound_cooldown = SERVER.HOSE_PULLEY_SOUND_COOLDOWN.get();
    }

    public static void reloadClient() {
        keep_flying_on_ground = CLIENT.KEEP_FLYING_ON_GROUND.get();
        toolbelt_hold_delay = CLIENT.TOOLBELT_HOLD_DELAY.get();
        toolbelt_animation_ticks = CLIENT.TOOLBELT_ANIMATION_TICKS.get();
        quick_unequip_items = CLIENT.QUICK_UNEQUIP_ITEMS.get();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            reloadServer();
            serverConfig = event.getConfig();
        } else if (event.getConfig().getSpec() == CLIENT_SPEC) {
            reloadClient();
            clientConfig = event.getConfig();
        }
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            reloadServer();
            serverConfig = event.getConfig();
        } else if (event.getConfig().getSpec() == CLIENT_SPEC) {
            reloadClient();
            clientConfig = event.getConfig();
        }
    }
}
