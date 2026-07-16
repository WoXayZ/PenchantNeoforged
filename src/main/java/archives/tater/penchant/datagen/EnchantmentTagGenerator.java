package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagGenerator extends PenchantTagsProvider<Enchantment> {

    private static final TagKey<Enchantment> HIDDEN_FROM_RECIPE_VIEWERS = TagKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("c", "hidden_from_recipe_viewers")
    );

    public EnchantmentTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENCHANTMENT, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(PenchantEnchantmentTags.DISABLED);

        builder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .remove(PenchantEnchantmentTags.DISABLED);
        builder(EnchantmentTags.TRADEABLE)
                .remove(PenchantEnchantmentTags.DISABLED);
        builder(EnchantmentTags.ON_RANDOM_LOOT)
                .remove(PenchantEnchantmentTags.DISABLED);
        builder(EnchantmentTags.ON_TRADED_EQUIPMENT)
                .remove(PenchantEnchantmentTags.DISABLED);
        builder(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                .remove(PenchantEnchantmentTags.DISABLED);

        builder(HIDDEN_FROM_RECIPE_VIEWERS)
                .addTag(PenchantEnchantmentTags.DISABLED);
    }
}
