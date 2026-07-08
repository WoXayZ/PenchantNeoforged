package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantFlag;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public class FlagTagGenerator extends PenchantTagsProvider<PenchantFlag> {
    private final PenchantFlag[] flags;

    public FlagTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, PenchantFlag... flags) {
        super(output, PenchantFlag.REGISTRY_KEY, registriesFuture);
        this.flags = flags;
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var builder = builder(PenchantFlag.ENABLED);
        for (var flag : flags) {
            builder.add(PenchantFlag.REGISTRY.getResourceKey(flag).orElseThrow());
        }
    }
}
