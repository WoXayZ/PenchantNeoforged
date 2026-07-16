package archives.tater.penchant.datagen;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.registry.PenchantBlockTags;

import net.minecraft.references.BlockItemIds;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {
    public BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture, Penchant.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        tag(PenchantBlockTags.DISENCHANTER)
                .add(BlockItemIds.GRINDSTONE.block());
    }
}
