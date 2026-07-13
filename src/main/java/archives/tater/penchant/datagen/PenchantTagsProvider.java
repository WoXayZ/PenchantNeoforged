package archives.tater.penchant.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Base {@link TagsProvider} that mirrors Fabric's {@code FabricTagsProvider.builder(...)} convenience
 * so the ported generators read almost identically to the originals.
 */
public abstract class PenchantTagsProvider<T> extends TagsProvider<T> {
    protected PenchantTagsProvider(PackOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, registryKey, lookupProvider, "penchant", existingFileHelper);
    }

    protected TagAppender<T> builder(TagKey<T> tag) {
        return this.tag(tag);
    }
}
