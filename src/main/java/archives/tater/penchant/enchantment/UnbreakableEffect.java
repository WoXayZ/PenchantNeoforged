package archives.tater.penchant.enchantment;

import archives.tater.penchant.registry.PenchantEnchantments;
import archives.tater.penchant.util.PenchantmentHelper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.Optional;

public record UnbreakableEffect(MinMaxBounds.Ints level, ItemPredicate item) {

    public UnbreakableEffect() { this(MinMaxBounds.Ints.ANY, ANY_ITEM); }
    public UnbreakableEffect(MinMaxBounds.Ints level) { this(level, ANY_ITEM); }
    public UnbreakableEffect(ItemPredicate item) { this(MinMaxBounds.Ints.ANY, item); }

    public boolean test(ItemStack stack, int level) {
        return this.level.matches(level) && item.test(stack);
    }

    public static boolean isUnbreakable(ItemStack stack) {
        var itemEnchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (var entry : itemEnchantments.entrySet()) {
            int level = entry.getIntValue();
            int max = PenchantmentHelper.getMaxLevel(entry.getKey());
            for (var effect : entry.getKey().value().getEffects(PenchantEnchantments.UNBREAKABLE))
                if (effect.test(stack, level) && level >= max)
                    return true;
        }
        return false;
    }

    /**
     * Ignore chance for one durability point, matching the datapack {@code remove_binomial}
     * curve {@code (level - 1) / 4} when max is 5, and scaled to any raised cap.
     */
    public static float ignoreChance(int level, int maxLevel) {
        if (maxLevel <= 1 || level >= maxLevel) return 1f;
        if (level <= 1) return 0f;
        return (float) (level - 1) / (maxLevel - 1);
    }

    public static void applyDurabilityIgnore(Enchantment enchantment, int level, RandomSource random, MutableFloat damage) {
        float chance = ignoreChance(level, PenchantmentHelper.getMaxLevel(enchantment));
        if (chance <= 0) return;
        float value = damage.floatValue();
        int skipped = 0;
        for (int i = 0; (float) i < value; i++) {
            if (random.nextFloat() < chance) skipped++;
        }
        damage.setValue(value - skipped);
    }

    public static final ItemPredicate ANY_ITEM = new ItemPredicate(Optional.empty(), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);

    public static final Codec<UnbreakableEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MinMaxBounds.Ints.CODEC.optionalFieldOf("level", MinMaxBounds.Ints.ANY).forGetter(UnbreakableEffect::level),
            ItemPredicate.CODEC.optionalFieldOf("item", ANY_ITEM).forGetter(UnbreakableEffect::item)
    ).apply(instance, UnbreakableEffect::new));
}
