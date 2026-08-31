package net.apertyotis.createandesiteabound.content.toolbox;

import com.simibubi.create.AllKeys;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import net.apertyotis.createandesiteabound.AllConfig;
import net.minecraft.client.Minecraft;

public class BetterToolboxHandlerClient {
    public static final BetterToolboxHandlerClient BETTER_TOOLBOX_HANDLER_CLIENT = new BetterToolboxHandlerClient();

    public boolean keyPressed = false;
    public boolean keyBlocked = false;
    public int holdTicks = 0;

    public void tick() {
        if (Minecraft.getInstance().screen != null) {
            keyPressed = false;
            holdTicks = 0;
            return;
        }
        if (keyBlocked)
            return;
        if (keyPressed) {
            if (holdTicks++ >= AllConfig.toolbelt_hold_delay) {
                keyPressed = false;
                keyBlocked = true;
                holdTicks = 0;
                ToolboxHandlerClient.onKeyInput(AllKeys.TOOLBELT.getBoundCode(), true);
            }
        }
    }

    public void onMouseInput(int ignored1, boolean ignored2) {
        if (keyPressed) {
            keyBlocked = true;
        }
    }
}
