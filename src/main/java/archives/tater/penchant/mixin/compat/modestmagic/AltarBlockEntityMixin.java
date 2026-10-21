package archives.tater.penchant.mixin.compat.modestmagic;

import net.minecraft.world.item.enchantment.Enchantment;

import com.baisylia.modestmagic.block.entity.custom.AltarBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Altar enchanting applies level 1. Higher levels come from using the equipment.
 */
@Mixin(AltarBlockEntity.class)
public class AltarBlockEntityMixin {
    @Redirect(method = "tryCraft", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    private int penchant$levelOne(Enchantment enchantment) {
        return 1;
    }
}
