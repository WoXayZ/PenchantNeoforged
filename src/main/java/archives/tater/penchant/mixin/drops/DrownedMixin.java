package archives.tater.penchant.mixin.drops;

import archives.tater.penchant.registry.PenchantFlag;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Drowned.class)
public class DrownedMixin {
    @WrapOperation(
            method = "populateDefaultEquipmentSlots",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Drowned;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V")
    )
    private void penchant$guaranteeTrident(Drowned instance, EquipmentSlot slot, ItemStack stack, Operation<Void> original) {
        original.call(instance, slot, stack);
        if (PenchantFlag.GUARANTEED_TRIDENT_DROP.isEnabled() && stack.is(Items.TRIDENT)) {
            instance.setDropChance(slot, 1f);
        }
    }
}
