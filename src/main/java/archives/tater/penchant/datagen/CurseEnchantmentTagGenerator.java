package archives.tater.penchant.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class CurseEnchantmentTagGenerator extends PenchantTagsProvider<Enchantment> {
    public CurseEnchantmentTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENCHANTMENT, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(EnchantmentTags.TRADEABLE)
                .remove(EnchantmentTags.CURSE);
        builder(EnchantmentTags.ON_RANDOM_LOOT)
                .remove(EnchantmentTags.CURSE);
        builder(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                .addTag(EnchantmentTags.CURSE);
    }
}
