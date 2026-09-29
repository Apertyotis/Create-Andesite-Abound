package net.apertyotis.createandesiteabound;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAndesiteAbound.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllKey {
    public static final KeyMapping NOTE_KEY = new KeyMapping("caa.keybind.open_note", InputConstants.KEY_LCONTROL, "caa.keybind.category");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(NOTE_KEY);
    }
}
