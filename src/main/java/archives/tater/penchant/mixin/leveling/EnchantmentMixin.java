package archives.tater.penchant.mixin.leveling;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hide Roman numerals for levelable enchantments (levels are shown via progress tooltips instead).
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "getFullname(I)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private void penchant$hideRomanNumeral(int level, CallbackInfoReturnable<Component> cir) {
        Enchantment self = (Enchantment) (Object) this;
        if (PenchantEnchantmentTags.isNoLeveling(self)) return;

        MutableComponent name = Component.translatable(self.getDescriptionId());
        if (self.isCurse()) {
            name.withStyle(ChatFormatting.RED);
        } else {
            name.withStyle(ChatFormatting.GRAY);
        }
        cir.setReturnValue(name);
    }
}
