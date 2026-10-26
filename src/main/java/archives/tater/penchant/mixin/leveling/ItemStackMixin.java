package archives.tater.penchant.mixin.leveling;

import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.enchantment.UnbreakingRework;
import archives.tater.penchant.registry.PenchantItemTags;
import archives.tater.penchant.util.PenchantmentHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    private void penchant$unbreakingV(CallbackInfoReturnable<Boolean> cir) {
        if (UnbreakingRework.isUnbreakable((ItemStack) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurtAndBreak", at = @At("HEAD"))
    private <T extends LivingEntity> void penchant$updateProgress(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        if (!entity.level().isClientSide && entity instanceof ServerPlayer player) {
            EnchantmentProgress.onDurabilityDamage((ItemStack) (Object) this, amount, player);
        }
    }

    @ModifyVariable(method = "enchant(Lnet/minecraft/world/item/enchantment/Enchantment;I)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int penchant$forceLevel(int level, Enchantment enchantment) {
        ItemStack self = (ItemStack) (Object) this;
        // Enchanted books keep level 1 for stored enchantments applied via enchant()
        if (self.is(net.minecraft.world.item.Items.ENCHANTED_BOOK) || self.is(net.minecraft.world.item.Items.BOOK)) {
            return 1;
        }
        if (self.is(PenchantItemTags.MAX_LEVEL_ENCHANTMENTS)) {
            return PenchantmentHelper.getMaxLevel(enchantment);
        }
        return 1;
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void penchant$levelUpUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (level.isClientSide || !((ItemStack) (Object) this).is(PenchantItemTags.LEVEL_UP_BY_USE)) return;
        if (!consumes(cir.getReturnValue().getResult())) return;
        EnchantmentProgress.addToProgress((ItemStack) (Object) this, 1, player);
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void penchant$levelUpUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel().isClientSide || !((ItemStack) (Object) this).is(PenchantItemTags.LEVEL_UP_BY_USE)) return;
        if (!consumes(cir.getReturnValue())) return;
        EnchantmentProgress.addToProgress((ItemStack) (Object) this, 1, context.getPlayer());
    }

    private static boolean consumes(InteractionResult result) {
        return result == InteractionResult.SUCCESS || result == InteractionResult.CONSUME || result == InteractionResult.CONSUME_PARTIAL;
    }
}
