package archives.tater.penchant.datagen;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.advancement.ExtractEnchantmentTrigger;
import archives.tater.penchant.advancement.OpenTableTrigger;
import archives.tater.penchant.registry.PenchantAdvancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.Optional;

import static archives.tater.penchant.datagen.DatagenUtil.createEmptyAdvancement;
import static archives.tater.penchant.datagen.DatagenUtil.registerAdvancement;

public class TableAdvancementGenerator extends AdvancementSubProvider {
    public TableAdvancementGenerator(BootstrapContext<Advancement> output) {
        super(output);
    }

    public static final Identifier ENCHANTED_BOOKSHELF = Penchant.id("enchanted_bookshelf");
    public static final Identifier FULL_LIBRARY = Penchant.id("full_library");
    public static final Identifier BABEL = Penchant.id("babel");
    public static final Identifier EXTRACT_ENCHANTMENT = Penchant.id("extract_enchantment");

    @Override
    public void generate() {
        var enchanter = createEmptyAdvancement(Identifier.withDefaultNamespace("story/enchant_item"));

        var enchantedBookshelf = registerAdvancement(ENCHANTED_BOOKSHELF, Items.CHISELED_BOOKSHELF, AdvancementType.TASK, this.output, builder -> builder
                .parent(enchanter)
                .addCriterion("has_enchantment", PenchantAdvancements.OPEN_TABLE.createCriterion(
                        new OpenTableTrigger.TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY, Optional.empty(), true)
                ))
        );

        var fullLibrary = registerAdvancement(FULL_LIBRARY, Items.BOOKSHELF, AdvancementType.GOAL, this.output, builder -> builder
                .parent(enchanter)
                .addCriterion("has_books", PenchantAdvancements.OPEN_TABLE.createCriterion(
                        new OpenTableTrigger.TriggerInstance(Optional.empty(), MinMaxBounds.Ints.atLeast(45), Optional.empty(), false)
                ))
        );

        registerAdvancement(BABEL, Items.BOOKSHELF, AdvancementType.CHALLENGE, this.output, builder -> builder
                .parent(fullLibrary)
                .addCriterion("has_books", PenchantAdvancements.OPEN_TABLE.createCriterion(
                        new OpenTableTrigger.TriggerInstance(Optional.empty(), MinMaxBounds.Ints.atLeast(300), Optional.empty(), false)
                ))
                .rewards(new AdvancementRewards.Builder()
                        .addExperience(100)
                )
        );

        registerAdvancement(EXTRACT_ENCHANTMENT, Items.GRINDSTONE, AdvancementType.TASK, this.output, builder -> builder
                .parent(enchanter)
                .addCriterion("extract", PenchantAdvancements.EXTRACT_ENCHANTMENT.createCriterion(
                        new ExtractEnchantmentTrigger.TriggerInstance(Optional.empty(), ItemPredicate.Builder.item().build(), Optional.empty())
                ))
        );
    }
}
