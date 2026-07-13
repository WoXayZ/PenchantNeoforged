package archives.tater.penchant.mixin.leveling;

import archives.tater.penchant.component.EnchantmentProgress;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

@Mixin(EnchantRandomlyFunction.class)
public class EnchantRandomlyFunctionMixin {
    @ModifyReturnValue(
            method = "run",
            at = @At("RETURN")
    )
    private ItemStack addProgress(ItemStack original, ItemStack stack, LootContext context) {
        if (EnchantmentHelper.getComponentType(original) == DataComponents.STORED_ENCHANTMENTS) return original;

        EnchantmentProgress.addRandomProgress(original, context.getRandom());

        return original;
    }
}
