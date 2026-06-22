package net.apertyotis.createandesiteabound;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.apertyotis.createandesiteabound.content.schematic.deploy.SimpleSchematicItem;
import net.minecraft.world.item.Item;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.REGISTRATE;


public class AllItems {

    public static final ItemEntry<SimpleSchematicItem> SIMPLE_SCHEMATIC =
        REGISTRATE.item("simple_schematic", SimpleSchematicItem::new)
            .properties(p -> p.stacksTo(16))
            .register();

    public static final ItemEntry<Item> SIMPLE_PACKER =
        REGISTRATE.item("simple_packer", Item::new)
            .properties(p -> p.stacksTo(1))
            .register();

    public static void register() {}
}
