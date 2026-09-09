package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class DropEnchantmentTagGenerator extends PenchantTagsProvider<Enchantment> {
    public DropEnchantmentTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENCHANTMENT, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // The category tags live in other packs, so they can only be referenced optionally from here.
        builder(PenchantEnchantmentTags.IGNORE_GUARANTEED_DROP)
                .addOptionalTag(PenchantEnchantmentTags.COMMON);
    }
}
