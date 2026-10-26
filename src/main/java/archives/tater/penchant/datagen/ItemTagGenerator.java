package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantItemTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends PenchantTagsProvider<Item> {

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public ItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, ExistingFileHelper existingFileHelper) {
        super(output, Registries.ITEM, registriesFuture, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        builder(PenchantItemTags.MAX_LEVEL_ENCHANTMENTS)
                .addOptionalTag(itemTag("c", "armors/horse"))
                .addOptionalTag(itemTag("c", "armors/nautilus"));

        builder(PenchantItemTags.PROPORTIONAL_ENCHANTMENT_PROGRESS)
                .addTag(ItemTags.ARMOR_ENCHANTABLE)
                .addTag(itemTag("c", "tools/shield"));

        builder(PenchantItemTags.LEVEL_UP_BY_KILLING)
                .addTag(ItemTags.ARMOR_ENCHANTABLE)
                .addTag(itemTag("c", "tools/shield"));

        builder(PenchantItemTags.DEFAULT_PROGRESS_COST_FACTOR_ONE)
                .addTag(itemTag("c", "tools/bow"))
                .addTag(itemTag("c", "tools/crossbow"));

        builder(PenchantItemTags.LEVEL_UP_BY_USE)
                .addOptional(ResourceLocation.fromNamespaceAndPath("enderscape", "mirror"));
    }
}
