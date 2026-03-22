package net.apertyotis.createandesiteabound;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.apertyotis.createandesiteabound.content.schematic.SimpleSchematicItem;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.REGISTRATE;


public class AllItems {

    public static final ItemEntry<SimpleSchematicItem> SIMPLE_SCHEMATIC =
            REGISTRATE.item("simple_schematic", SimpleSchematicItem::new)
                    .properties(p -> p.stacksTo(16))
                    .register();

    public static void register() {}
}
