package archives.tater.penchant.mixin.leveling;

import archives.tater.penchant.component.EnchantmentProgress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    // In 26.1.2 the ServerPlayer overload of hurtAndBreak only delegates to this LivingEntity overload, which is the
    // central implementation every durability path (combat, mining, armor...) funnels through. Injecting here ensures
    // that hitting mobs actually grants enchantment progress. We keep the original player-only leveling behavior.
    @Inject(
            at = @At("HEAD"),
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"
    )
	private void updateProgress(int amount, ServerLevel level, @Nullable LivingEntity player, Consumer<Item> onBreak, CallbackInfo ci) {
        if (player instanceof ServerPlayer)
            EnchantmentProgress.onDurabilityDamage((ItemStack) (Object) this, player);
	}

    @Inject(
            at = @At("HEAD"),
            method = "hurtWithoutBreaking"
    )
    private void updateProgress(int amount, Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer)
            EnchantmentProgress.onDurabilityDamage((ItemStack) (Object) this, player);
    }
}
