package net.apertyotis.createandesiteabound;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateAndesiteAbound.MOD_ID)
public class CreateAndesiteAbound {
    public static final String MOD_ID = "createandesiteabound";

    static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public CreateAndesiteAbound() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        REGISTRATE.registerEventListeners(modEventBus);

        AllBlocks.register();
        AllBlockEntityType.register();

        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AllConfig.COMMON_SPEC);
    }
}