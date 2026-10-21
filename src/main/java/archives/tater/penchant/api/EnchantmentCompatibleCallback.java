package archives.tater.penchant.api;

import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.util.PenchantmentHelper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event to modify when a {@link PenchantmentMenu} marks enchantments as incompatible.
 * <p>
 * This behavior falls back to {@link PenchantmentHelper#areCompatible(Enchantment, Enchantment)}, which checks
 * vanilla exclusivity and Max Protection, so if you've set incompatibilities with a mixin there you don't need to
 * do it here.
 * <p>
 * A {@link TriState#FALSE} result also blocks the enchantment and shows the same incompatibility message vanilla
 * exclusivity uses.
 */
@FunctionalInterface
public interface EnchantmentCompatibleCallback {
    /**
     * @param enchantment the enchantment attempted to be added
     * @param existing    an enchantment already on the stack
     * @param stack       the stack under consideration
     * @return whether {@code enchantment} is compatible with {@code existing}, or {@link TriState#DEFAULT} to
     * fall back to other handlers
     */
    TriState areCompatible(Enchantment enchantment, Enchantment existing, ItemStack stack);

    /**
     * Listeners are queried in registration order; the first non-{@link TriState#DEFAULT} result wins.
     */
    final class Event {
        private final List<EnchantmentCompatibleCallback> listeners = new CopyOnWriteArrayList<>();

        public void register(EnchantmentCompatibleCallback listener) {
            listeners.add(listener);
        }

        public TriState invoke(Enchantment enchantment, Enchantment existing, ItemStack stack) {
            for (var listener : listeners) {
                var result = listener.areCompatible(enchantment, existing, stack);
                if (result != TriState.DEFAULT)
                    return result;
            }
            return TriState.DEFAULT;
        }
    }

    Event EVENT = new Event();
}
