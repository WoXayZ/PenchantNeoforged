package archives.tater.penchant.mixin.loot;

import archives.tater.penchant.util.PenchantLoot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(EnchantRandomlyFunction.class)
public class EnchantRandomlyFunctionMixin {
    /**
     * When the function builds its own discoverable list, filter by loot-rework categories.
     */
    @ModifyExpressionValue(
            method = "run",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;")
    )
    private Object penchant$filterCollected(Object collected, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) LootContext context) {
        if (!(collected instanceof List<?> list)) return collected;
        @SuppressWarnings("unchecked")
        List<Enchantment> enchantments = (List<Enchantment>) list;
        return enchantments.stream()
                .filter(enchantment -> PenchantLoot.isAllowed(stack, enchantment))
                .collect(Collectors.toList());
    }
}
