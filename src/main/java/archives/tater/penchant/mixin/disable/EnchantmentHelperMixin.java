package archives.tater.penchant.mixin.disable;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    // Vanilla/Fabric filtered enchantments with a boolean lambda we could @ModifyReturnValue on. NeoForge rewrote
    // getAvailableEnchantmentResults to use a method reference (itemStack::isPrimaryItemFor), so we instead wrap the
    // Stream.filter call and compose an extra predicate that drops enchantments tagged as disabled.
    @WrapOperation(
            method = "getAvailableEnchantmentResults",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;")
    )
    private static Stream<Holder<Enchantment>> disableEnchantment(Stream<Holder<Enchantment>> stream, Predicate<Holder<Enchantment>> predicate, Operation<Stream<Holder<Enchantment>>> original) {
        return original.call(stream, predicate.and(enchantment -> !enchantment.is(PenchantEnchantmentTags.DISABLED)));
    }
}
