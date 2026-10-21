package archives.tater.penchant.mixin.bookshelf;

import archives.tater.penchant.PenchantCompat;
import archives.tater.penchant.registry.PenchantFlag;
import archives.tater.penchant.util.PenchantmentHelper;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.objectweb.asm.Opcodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
    // NeoForge patches the vanilla `.is(ENCHANTMENT_POWER_PROVIDER)` check on the shelf block into a call to the
    // Forge enchant-power API (`getEnchantPowerBonus`), so we wrap that operation instead of the tag lookup.
    @Definition(id = "getEnchantPowerBonus", method = "Lnet/minecraft/world/level/block/state/BlockState;getEnchantPowerBonus(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)F")
    @Expression("?.getEnchantPowerBonus(?, ?)")
    @WrapOperation(
            method = "isValidBookShelf",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private static float checkChiseled(BlockState instance, LevelReader level, BlockPos pos, Operation<Float> original) {
        float power = original.call(instance, level, pos);
        if (power <= 0f) return 0f;
        if (PenchantmentHelper.getBookCount(level, pos) <= 0f) return 0f;
        var block = level.getBlockState(pos).getBlock();
        // 26.1.2 reports a fraction of a shelf per book on a lectern or chiseled bookshelf.
        // The vanilla table should count a non-empty one as a single shelf, matching 26.2+.
        if (block instanceof ChiseledBookShelfBlock || block instanceof LecternBlock) return 1f;
        return power;
    }

    @ModifyExpressionValue(
            method = "animateTick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/EnchantingTableBlock;BOOKSHELF_OFFSETS:Ljava/util/List;", opcode = Opcodes.GETSTATIC)
    )
    private List<BlockPos> lenientBookshelfPlacement(List<BlockPos> original) {
        // Enchiridion's table draws a particle for every offset, even when no bookshelf is there.
        return PenchantCompat.ENCHIRIDION_INSTALLED ? original : PenchantmentHelper.getBookshelfOffsets(original);
    }

    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z")
    @Definition(id = "ENCHANTMENT_POWER_TRANSMITTER", field = "Lnet/minecraft/tags/BlockTags;ENCHANTMENT_POWER_TRANSMITTER:Lnet/minecraft/tags/TagKey;")
    @Expression("?.is(ENCHANTMENT_POWER_TRANSMITTER)")
    @ModifyExpressionValue(
            method = "isValidBookShelf",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private static boolean allowObstruction(boolean original) {
        return original || PenchantFlag.LENIENT_BOOKSHELF_PLACEMENT.isEnabled();
    }
}
