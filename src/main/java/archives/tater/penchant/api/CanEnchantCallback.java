package archives.tater.penchant.api;

import archives.tater.penchant.menu.PenchantmentMenu;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Callback to modify when items can be enchanted in a {@link PenchantmentMenu}.
 * <p>
 * Penchant already checks {@code ItemStack.supportsEnchantment} (NeoForge's item enchantability hook), so if
 * you've enabled enchanting through that API you don't need to do it here.
 */
@FunctionalInterface
public interface CanEnchantCallback {
    TriState canEnchant(ItemStack stack, Holder<Enchantment> enchantment);

    /**
     * A simple array-backed event. Listeners are queried in registration order; the first non-{@link TriState#DEFAULT}
     * result wins. Replaces Fabric's {@code Event}/{@code EventFactory} to keep the addon-facing API dependency-free.
     */
    final class Event {
        private final List<CanEnchantCallback> listeners = new CopyOnWriteArrayList<>();

        public void register(CanEnchantCallback listener) {
            listeners.add(listener);
        }

        public TriState invoke(ItemStack stack, Holder<Enchantment> enchantment) {
            for (var listener : listeners) {
                var result = listener.canEnchant(stack, enchantment);
                if (result != TriState.DEFAULT)
                    return result;
            }
            return TriState.DEFAULT;
        }
    }

    /**
     * If an item should normally be enchantable with a certain enchantment, ignoring compatibility with other enchantments
     */
    Event ITEM = new Event();

    /**
     * If a particular stack should be enchantable with a certain enchantment, should only be used to check for more specific cases such as component data
     */
    Event STACK = new Event();
}
