package archives.tater.penchant.mixin.leveling;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.item.CreativeModeTabs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Creative inventory: only one book per enchantment (level 1), matching Penchant leveling.
 */
@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {
    @ModifyExpressionValue(
            method = "lambda$generateEnchantmentBookTypesAllLevels$43",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I")
    )
    private static int penchant$onlyLevelOne(int maxLevel) {
        return 1;
    }
}
