package archives.tater.penchant.enchantment;

import archives.tater.penchant.registry.PenchantFlag;
import archives.tater.penchant.util.PenchantmentHelper;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Durability Rework (1.20.1): Unbreaking has 5 levels. Ignore chance is
 * {@code (level - 1) / 4} (I 0%, II 25%, III 50%, IV 75%, V 100%), matching the
 * 1.21+ datapack {@code remove_binomial} curve. The last Unbreaking level makes the item unbreakable.
 */
public final class UnbreakingRework {
    public static final int MAX_LEVEL = 5;

    private UnbreakingRework() {}

    public static boolean isModuleEnabled() {
        return PenchantFlag.DURABILITY_REWORK.isEnabled();
    }

    public static boolean isUnbreakable(ItemStack stack) {
        if (!isModuleEnabled() || stack.isEmpty()) return false;
        int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, stack);
        return level >= PenchantmentHelper.getMaxLevel(Enchantments.UNBREAKING);
    }

    /** {@code true} means this durability point is skipped. */
    public static boolean shouldIgnoreDurabilityDrop(int level, RandomSource random) {
        int max = PenchantmentHelper.getMaxLevel(Enchantments.UNBREAKING);
        if (level >= max) return true;
        if (level <= 1 || max <= 1) return false;
        return random.nextInt(max - 1) < (level - 1);
    }
}
