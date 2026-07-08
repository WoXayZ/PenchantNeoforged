package archives.tater.penchant.mixin.leveling;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuMixin {
    // Vanilla/Fabric called ItemStack.enchant(Holder, int) directly in the click lambda; NeoForge routes enchanting
    // through Item.applyEnchantments(stack, list) instead. We therefore rewrite the enchantment list that feeds it,
    // forcing every enchantment to level 1 (Penchant's core "level up through usage" mechanic) unless tagged NO_LEVELING.
    @WrapOperation(
            method = "lambda$clickMenuButton$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/EnchantmentMenu;getEnchantmentList(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/ItemStack;II)Ljava/util/List;")
    )
    private List<EnchantmentInstance> levelOne(EnchantmentMenu instance, RegistryAccess access, ItemStack itemStack, int slot, int cost, Operation<List<EnchantmentInstance>> original) {
        List<EnchantmentInstance> list = original.call(instance, access, itemStack, slot, cost);
        return list.stream()
                .map(entry -> entry.enchantment().is(PenchantEnchantmentTags.NO_LEVELING)
                        ? entry
                        : new EnchantmentInstance(entry.enchantment(), 1))
                .toList();
    }
}
