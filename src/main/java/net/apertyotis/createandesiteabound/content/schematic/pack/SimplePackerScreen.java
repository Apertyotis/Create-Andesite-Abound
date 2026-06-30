package net.apertyotis.createandesiteabound.content.schematic.pack;

import net.apertyotis.createandesiteabound.AllItems;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class SimplePackerScreen extends AbstractSimiScreen {

    private final AllGuiTextures background;
    private final ItemStack packer;

    private final Component abortLabel = Lang.translateDirect("action.discard");
    private final Component confirmLabel = Lang.translateDirect("action.saveToFile");
    private EditBox nameField;


    public SimplePackerScreen() {
        super(Lang.translateDirect("schematicAndQuill.title"));
        background = AllGuiTextures.SCHEMATIC_PROMPT;
        packer = AllItems.SIMPLE_PACKER.asStack();
        SimplePackerItem.randomVariant(packer);
    }

    @Override
    public void init() {
        setWindowSize(background.width, background.height);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        nameField = new EditBox(font, x + 49, y + 26, 131, 10, Components.immutableEmpty());
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
        nameField.setMaxLength(35);
        nameField.setFocused(true);
        setFocused(nameField);

        IconButton abort = new IconButton(x + 158, y + 53, AllIcons.I_TRASH);
        abort.withCallback(this::onClose);
        abort.setToolTip(abortLabel);

        IconButton confirm = new IconButton(x + 180, y + 53, AllIcons.I_CONFIRM);
        confirm.withCallback(this::confirm);
        confirm.setToolTip(confirmLabel);
        addRenderableWidgets(nameField, abort, confirm);

    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        graphics.blit(background.location, x + 7, y + 53, 26, 53, 19, 19);
        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

        GuiGameElement.of(AllItems.SIMPLE_SCHEMATIC.asStack())
            .at(x + 22, y + 22, 0)
            .render(graphics);

        GuiGameElement.of(packer)
            .scale(3)
            .at(x + background.width + 6, y + background.height - 40, -200)
            .render(graphics);
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            confirm();
            return true;
        }
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }
        return nameField.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    private void confirm() {
        SimplePackerHandler.SIMPLE_PACKER_HANDLER.confirm(nameField.getValue());
        onClose();
    }
}
