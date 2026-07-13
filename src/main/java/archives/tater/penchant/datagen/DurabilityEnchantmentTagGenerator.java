package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class DurabilityEnchantmentTagGenerator extends PenchantTagsProvider<Enchantment> {
    public DurabilityEnchantmentTagGenerator(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.ENCHANTMENT, lookupProvider, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
        builder(PenchantEnchantmentTags.DISABLED).add(Enchantments.MENDING);
    }
}
