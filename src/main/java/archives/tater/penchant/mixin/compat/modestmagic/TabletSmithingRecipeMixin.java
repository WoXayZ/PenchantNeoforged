package archives.tater.penchant.mixin.compat.modestmagic;

import net.minecraft.world.item.enchantment.Enchantment;

import com.baisylia.modestmagic.recipe.custom.TabletSmithingRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Tablet smithing applies level 1. Higher levels come from using the equipment.
 */
@Mixin(TabletSmithingRecipe.class)
public class TabletSmithingRecipeMixin {
    @Redirect(method = "assemble", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    private int penchant$levelOne(Enchantment enchantment) {
        return 1;
    }
}
