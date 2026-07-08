package archives.tater.penchant.datagen;

import archives.tater.penchant.Penchant;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static archives.tater.penchant.datagen.DatagenUtil.createEmptyAdvancement;
import static archives.tater.penchant.datagen.DatagenUtil.registerAdvancement;

public class LootAdvancementGenerator implements AdvancementSubProvider {

    public static final Identifier ALL_ENCHANTMENTS = Penchant.id("all_enchantments");

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
                            enchantment.key().identifier().toString(),
                            InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().withComponents(
                                    DataComponentMatchers.Builder.components().partial(
                                            DataComponentPredicates.STORED_ENCHANTMENTS,
                                            EnchantmentsPredicate.StoredEnchantments.storedEnchantments(List.of(
                                                    new EnchantmentPredicate(enchantment, MinMaxBounds.Ints.ANY)
                                            ))
                                    ).build()
                            ))
                    ));
        });
    }
}
