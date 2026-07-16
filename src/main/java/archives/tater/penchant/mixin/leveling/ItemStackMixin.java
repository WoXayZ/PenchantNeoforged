package archives.tater.penchant.mixin.leveling;

import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.registry.PenchantEnchantmentTags;
import archives.tater.penchant.registry.PenchantItemTags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    // NeoForge routes durability through the LivingEntity overload; the ServerPlayer overload is only a delegate.
    @Inject(
            at = @At("HEAD"),
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"
    )
    private void updateProgress(int amount, ServerLevel level, @Nullable LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player)
            EnchantmentProgress.onDurabilityDamage((ItemStack) (Object) this, player);
    }

    @Inject(
            at = @At("HEAD"),
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
    )
    private void updateArmorProgress(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player)
            EnchantmentProgress.onDurabilityDamage((ItemStack) (Object) this, player);
    }

    @ModifyVariable(
            at = @At("HEAD"),
            method = "enchant",
            argsOnly = true,
            ordinal = 0
    )
    private int forceLevel(int level, Holder<Enchantment> enchantment) {
        if (EnchantmentHelper.getComponentType((ItemStack) (Object) this) == DataComponents.STORED_ENCHANTMENTS) {
            return enchantment.is(PenchantEnchantmentTags.NO_LEVELING) ? level : 1;
        }
        if (((ItemStack) (Object) this).is(PenchantItemTags.MAX_LEVEL_ENCHANTMENTS))
            return enchantment.value().getMaxLevel();

        return level;
    }
}
