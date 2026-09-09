package archives.tater.penchant.util;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Call {@link Enchantment#getMaxLevel()} from outside {@code CreativeModeTabs} mixin
 * handlers so a {@code @Redirect} on that class cannot recurse into itself.
 */
public final class EnchantmentMaxLevelAccess {
    private EnchantmentMaxLevelAccess() {}

    public static int get(Enchantment enchantment) {
        return enchantment.getMaxLevel();
    }

    /**
     * Vanilla {@link Enchantment#getMaxLevel()} returns 1. Remove Enchantment Limits injects
     * that base method with a global cap, which subclass overrides (Protection 4, Unbreaking 3)
     * never see. If several vanilla 1-level enchantments all report the same other value, that
     * cap is in play.
     */
    public static int injectedGlobalCap() {
        int mending = Enchantments.MENDING.getMaxLevel();
        if (mending == 1) return 0;
        int aqua = Enchantments.AQUA_AFFINITY.getMaxLevel();
        int silk = Enchantments.SILK_TOUCH.getMaxLevel();
        return mending == aqua && mending == silk ? mending : 0;
    }
}
