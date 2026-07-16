package archives.tater.penchant.datagen;

import archives.tater.penchant.Penchant;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockTags;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BookshelfBlockTagGenerator extends BlockTagsProvider {
    public BookshelfBlockTagGenerator(PackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, registriesFuture, Penchant.MOD_ID);
    }

    @Override
    protected void addTags(Provider provider) {
        tag(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                .add(BlockItemIds.CHISELED_BOOKSHELF.block(), BlockItemIds.LECTERN.block())
                .addTag(Tags.Blocks.BOOKSHELVES);
    }
}
