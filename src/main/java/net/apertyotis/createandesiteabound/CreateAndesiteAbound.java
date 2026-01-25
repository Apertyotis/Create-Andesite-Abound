package net.apertyotis.createandesiteabound;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(CreateAndesiteAbound.MOD_ID)
public class CreateAndesiteAbound {
    public static final String MOD_ID = "createandesiteabound";

    public CreateAndesiteAbound() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }
}