package archives.tater.penchant.mixin.effect;

import archives.tater.penchant.enchantment.UnbreakingRework;
import archives.tater.penchant.registry.PenchantFlag;
import archives.tater.penchant.util.EnchantmentMaxLevelAccess;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DigDurabilityEnchantment.class)
public abstract class DigDurabilityEnchantmentMixin extends Enchantment {
    private DigDurabilityEnchantmentMixin(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots) {
        super(rarity, category, slots);
    }

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void penchant$unbreakingCap(CallbackInfoReturnable<Integer> cir) {
        if (!PenchantFlag.DURABILITY_REWORK.isEnabled()) return;
        // INVOKESPECIAL: REL injects Enchantment.getMaxLevel(), which the Unbreaking override skips.
        int parent = super.getMaxLevel();
        if (parent != 1) {
            cir.setReturnValue(parent);
            return;
        }
        int global = EnchantmentMaxLevelAccess.injectedGlobalCap();
        cir.setReturnValue(global > 0 ? global : UnbreakingRework.MAX_LEVEL);
    }

    @Inject(method = "shouldIgnoreDurabilityDrop", at = @At("HEAD"), cancellable = true)
    private static void penchant$reworkCurve(ItemStack stack, int level, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (!PenchantFlag.DURABILITY_REWORK.isEnabled()) return;
        cir.setReturnValue(UnbreakingRework.shouldIgnoreDurabilityDrop(level, random));
    }
}
