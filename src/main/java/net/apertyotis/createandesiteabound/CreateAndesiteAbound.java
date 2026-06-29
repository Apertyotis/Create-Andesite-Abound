package net.apertyotis.createandesiteabound;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateAndesiteAbound.MOD_ID)
public class CreateAndesiteAbound {
    public static final String MOD_ID = "createandesiteabound";

    public static final Logger LOGGER = LogUtils.getLogger();

    static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    static {
        REGISTRATE.setTooltipModifierFactory(item ->
            new ItemDescription.Modifier(item, Palette.STANDARD_CREATE));
    }

    @SuppressWarnings("removal")
    public CreateAndesiteAbound() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        REGISTRATE.registerEventListeners(modEventBus);

        AllItems.register();
        AllBlocks.register();
        AllBlockEntityType.register();
        AllPackets.registerPackets();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AllConfig.COMMON_SPEC);
    }
}