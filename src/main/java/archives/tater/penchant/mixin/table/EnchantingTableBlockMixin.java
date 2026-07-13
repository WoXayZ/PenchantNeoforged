package archives.tater.penchant.mixin.table;

import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.registry.PenchantFlag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
    @ModifyReturnValue(method = "getMenuProvider", at = @At("RETURN"))
    private MenuProvider replaceMenuProvider(MenuProvider original, BlockState state, Level level, BlockPos pos) {
        if (!PenchantFlag.REWORKED_TABLE_MENU.isEnabled()) return original;
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return original.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                return new PenchantmentMenu(syncId, inventory, ContainerLevelAccess.create(level, pos));
            }
        };
    }

    @Inject(
            method = "useWithoutItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", shift = At.Shift.AFTER)
    )
    private void sendEnchantments(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player.containerMenu instanceof PenchantmentMenu penchantmentMenu)) return;
        penchantmentMenu.sendEnchantments();
    }
}
