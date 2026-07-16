package archives.tater.penchant.datagen;

import net.minecraft.data.DataProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackMetaGen {

    private PackMetaGen() {}

    public static DataProvider.Factory<PackMetadataGenerator> pack(Identifier id) {
        return pack(id, FeatureFlagSet.of());
    }

    public static DataProvider.Factory<PackMetadataGenerator> pack(Identifier id, FeatureFlagSet flags) {
        return output -> {
            var generator = PackMetadataGenerator.forFeaturePack(output, Component.translatable(id.toLanguageKey("dataPack", "description")), flags);
            if (!flags.isEmpty())
                generator.add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(flags));
            return generator;
        };
    }
}
