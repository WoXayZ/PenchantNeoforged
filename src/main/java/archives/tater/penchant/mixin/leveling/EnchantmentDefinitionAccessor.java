package archives.tater.penchant.mixin.leveling;

import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reads {@code maxLevel} straight off the record field, bypassing the {@code maxLevel()} accessor
 * that other mods routinely inject into.
 */
@Mixin(Enchantment.EnchantmentDefinition.class)
public interface EnchantmentDefinitionAccessor {
    @Accessor("maxLevel")
    int getPenchantMaxLevel();
}
