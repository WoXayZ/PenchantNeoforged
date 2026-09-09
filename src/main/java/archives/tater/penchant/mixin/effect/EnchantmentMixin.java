package archives.tater.penchant.mixin.effect;

import archives.tater.penchant.enchantment.UnbreakableEffect;
import archives.tater.penchant.registry.PenchantEnchantments;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Replaces the datapack {@code remove_binomial} (hardcoded {@code / 4}) with a curve scaled to the
 * runtime max, so Unbreaking is only unbreakable at its last level when another mod raises the cap.
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "modifyDurabilityChange", at = @At("HEAD"), cancellable = true)
    private void penchant$scaleUnbreakingCurve(ServerLevel level, int enchantmentLevel, ItemStack stack, MutableFloat damage, CallbackInfo ci) {
        var self = (Enchantment) (Object) this;
        if (self.getEffects(PenchantEnchantments.UNBREAKABLE).isEmpty()) return;
        UnbreakableEffect.applyDurabilityIgnore(self, enchantmentLevel, level.getRandom(), damage);
        ci.cancel();
    }
}
