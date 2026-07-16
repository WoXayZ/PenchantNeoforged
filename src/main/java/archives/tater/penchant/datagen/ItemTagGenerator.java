package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantItemTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends PenchantTagsProvider<Item> {

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public ItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ITEM, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        builder(PenchantItemTags.MAX_LEVEL_ENCHANTMENTS)
                .addOptionalTag(itemTag("c", "armors/horse"))
                .addOptionalTag(itemTag("c", "armors/nautilus"));
    }
}
