package archives.tater.penchant.client;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.registry.PenchantItemTags;
import archives.tater.penchant.util.PenchantmentHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Penchant.MOD_ID, value = Dist.CLIENT)
public class PenchantTooltips {
    private static final int BAR_WIDTH = 32;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(Items.ENCHANTED_BOOK) || stack.is(PenchantItemTags.MAX_LEVEL_ENCHANTMENTS)) return;

        Map<Enchantment, Integer> enchantments = PenchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) return;

        boolean showProgress = PenchantKeys.isShowProgressDown();
        List<Component> tooltip = event.getToolTip();

        if (!showProgress) {
            tooltip.add(Component.translatable("penchant.tooltip.progress.key", Component.keybind("key.penchant.show_progress"))
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        EnchantmentProgress progress = EnchantmentProgress.getProgress(stack);
        // Insert a progress line immediately under each enchantment name (NeoForge parity)
        List<Component> rebuilt = new ArrayList<>(tooltip.size() + enchantments.size());
        for (Component line : tooltip) {
            rebuilt.add(line);
            for (var entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                if (!EnchantmentProgress.shouldShowTooltip(enchantment)) continue;
                Component name = PenchantmentHelper.getName(enchantment);
                if (!line.getString().contains(name.getString())) continue;

                int level = entry.getValue();
                if (level >= enchantment.getMaxLevel()) {
                    rebuilt.add(Component.literal("  ")
                            .append(FontUtils.getBar(BAR_WIDTH, BAR_WIDTH))
                            .append(" ")
                            .append(Component.translatable("penchant.tooltip.progress.max"))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                } else {
                    int max = EnchantmentProgress.getMaxProgress(enchantment, level, stack.getMaxDamage());
                    // Uncapped datapack enchantments can store progress far past the bar;
                    // BAR_WIDTH * current overflows int and String.repeat crashes (JakeBreathild).
                    int current = Mth.clamp(progress.getProgress(enchantment), 0, Math.max(max, 0));
                    int filled = max <= 0 ? 0 : (int) ((long) BAR_WIDTH * current / max);
                    rebuilt.add(Component.literal("  ")
                            .append(FontUtils.getBar(BAR_WIDTH, filled))
                            .append(" ")
                            .append(Component.translatable("penchant.tooltip.progress",
                                    Component.literal(Integer.toString(current)).withStyle(ChatFormatting.LIGHT_PURPLE),
                                    max
                            ).withStyle(ChatFormatting.DARK_GRAY)));
                }
            }
        }
        tooltip.clear();
        tooltip.addAll(rebuilt);
    }
}
