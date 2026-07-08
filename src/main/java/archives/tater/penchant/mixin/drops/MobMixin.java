package archives.tater.penchant.mixin.drops;

import archives.tater.penchant.registry.PenchantFlag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    @Shadow
    public abstract void setDropChance(EquipmentSlot slot, float dropChance);

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "enchantSpawnedEquipment",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;enchantItemFromProvider(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/util/RandomSource;)V")
    )
    private void guaranteeDrop(ServerLevelAccessor level, EquipmentSlot slot, RandomSource random, float enchantChance, DifficultyInstance difficulty, CallbackInfo ci) {
        if (PenchantFlag.GUARANTEED_ENCHANTED_DROP.isEnabled())
            setDropChance(slot, 1f);
    }
}
