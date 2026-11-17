package net.apertyotis.createandesiteabound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

import static net.apertyotis.createandesiteabound.CreateAndesiteAbound.MOD_ID;

public class AllTags {
    public enum AllItemTags {
        GHOST_SUPPLY;

        public final TagKey<Item> tag;

        @SuppressWarnings("removal")
        AllItemTags() {
            ResourceLocation id = new ResourceLocation(MOD_ID, name().toLowerCase(Locale.ROOT));
            tag = ItemTags.create(id);
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Item item) {
            return item.builtInRegistryHolder()
                .is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack.is(tag);
        }

        public static void init() {}
    }

    public static void init() {
        AllItemTags.init();
    }
}
