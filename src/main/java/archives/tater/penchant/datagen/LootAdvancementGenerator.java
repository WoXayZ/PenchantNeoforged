package archives.tater.penchant.datagen;

import archives.tater.penchant.Penchant;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static archives.tater.penchant.datagen.DatagenUtil.createEmptyAdvancement;
import static archives.tater.penchant.datagen.DatagenUtil.registerAdvancement;

public class LootAdvancementGenerator implements AdvancementSubProvider {

    public static final ResourceLocation ALL_ENCHANTMENTS = Penchant.id("all_enchantments");

    @Override
    public void generate(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
        var enchantments = registryLookup.lookupOrThrow(Registries.ENCHANTMENT);
        var enchantedBookshelf = createEmptyAdvancement(TableAdvancementGenerator.ENCHANTED_BOOKSHELF);

        registerAdvancement(ALL_ENCHANTMENTS, Items.ENCHANTED_BOOK, AdvancementType.GOAL, consumer, builder -> {
            builder
                    .parent(enchantedBookshelf)
                    .requirements(AdvancementRequirements.Strategy.AND);

            Streams.concat(
                    LootEnchantmentTagGenerator.UNCOMMON.stream(),
                    LootEnchantmentTagGenerator.RARE.stream(),
                    Stream.of(
                            Enchantments.SWIFT_SNEAK,
                            Enchantments.SOUL_SPEED,
                            Enchantments.WIND_BURST
                    )
            )
                    .map(enchantments::getOrThrow)
                    .forEach(enchantment -> builder.addCriterion(
                            enchantment.unwrapKey().orElseThrow().location().toString(),
                            InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item()
                                    .withSubPredicate(
                                            ItemSubPredicates.STORED_ENCHANTMENTS,
                                            ItemEnchantmentsPredicate.storedEnchantments(List.of(
                                                    new EnchantmentPredicate(enchantment, MinMaxBounds.Ints.ANY)
                                            ))
                                    )
                                    .build())
                    ));
        });
    }
}
