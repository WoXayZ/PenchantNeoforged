package archives.tater.penchant;

import archives.tater.penchant.client.FontUtils;
import archives.tater.penchant.client.KeyMappingExt;
import archives.tater.penchant.client.PenchantClientConfig;
import archives.tater.penchant.client.gui.screen.PenchantmentScreen;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.enchantment.UnbreakableEffect;
import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.registry.PenchantItemTags;
import archives.tater.penchant.registry.PenchantMenus;
import archives.tater.penchant.util.PenchantmentHelper;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import static archives.tater.penchant.util.PenchantUtil.containsIgnoreStyle;
import static net.minecraft.Util.makeDescriptionId;
import static net.minecraft.util.Mth.clamp;

@Mod(value = Penchant.MOD_ID, dist = Dist.CLIENT)
public class PenchantClient {
    public static final String PENCHANT_CATEGORY = Penchant.MOD_ID;

    public static final KeyMappingExt SHOW_PROGRESS_KEYBIND = new KeyMappingExt(
            makeDescriptionId("key", Penchant.id("show_progress")),
            Type.KEYSYM,
            InputConstants.KEY_LCONTROL,
            PENCHANT_CATEGORY
    );

    @ApiStatus.Internal
    public static final ThreadLocal<@Nullable ItemStack> tooltipItem = new ThreadLocal<>();

    public PenchantClient(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, PenchantClientConfig.SPEC);

        modBus.addListener(this::onRegisterMenuScreens);
        modBus.addListener(this::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::onLoggingIn);
        // Runs last so the bars are placed against the finished tooltip, after mods such as Enchiridion
        // have restyled or reordered the enchantment lines.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PenchantClient::onItemTooltip);
    }

    private static void onItemTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        addUnbreakableTooltip(event.getToolTip(), stack);

        if (!shouldShowProgress()) return;

        if (stack.has(DataComponents.STORED_ENCHANTMENTS) || stack.is(PenchantItemTags.MAX_LEVEL_ENCHANTMENTS)) return;

        var enchantments = stack.getEnchantments();
        if (enchantments.isEmpty()) return;
        var progress = stack.getOrDefault(PenchantComponents.ENCHANTMENT_PROGRESS, EnchantmentProgress.EMPTY);

        for (var iter = event.getToolTip().listIterator(); iter.hasNext();) {
            var line = iter.next();

            for (var entry : enchantments.entrySet()) {
                var enchantment = entry.getKey();
                if (!EnchantmentProgress.shouldShowTooltip(enchantment)) continue;
                if (!containsIgnoreStyle(line, enchantment.value().description())) continue;
                iter.add(getProgressTooltip(progress, enchantment, entry.getIntValue(), stack));
                break;
            }
        }
    }


    /** Vanilla only prints Unbreakable when the component is present; Penchant's Unbreaking V effect never sets it. */
    private static void addUnbreakableTooltip(java.util.List<Component> tooltip, ItemStack stack) {
        if (!UnbreakableEffect.isUnbreakable(stack)) return;
        var label = Component.translatable("item.unbreakable");
        for (var line : tooltip) {
            if (containsIgnoreStyle(line, label) || line.getString().equals(label.getString())) return;
        }
        tooltip.add(Math.min(1, tooltip.size()), label.copy().withStyle(ChatFormatting.BLUE));
    }

    private void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(PenchantMenus.PENCHANTMENT_MENU, PenchantmentScreen::new);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SHOW_PROGRESS_KEYBIND);
    }

    private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        PenchantmentDefinition.buildCache(event.getPlayer().registryAccess());
    }

    public static boolean shouldShowProgress() {
        return PenchantClientConfig.ALWAYS_SHOW_TOOLTIP_PROGRESS.get() || SHOW_PROGRESS_KEYBIND.isDownAnywhere();
    }

    public static boolean shouldShowKeyHint() {
        return !shouldShowProgress() && PenchantClientConfig.SHOW_TOOLTIP_KEY_HINT.get();
    }

    public static int getBarWidth() {
        return PenchantClientConfig.BAR_WIDTH.get();
    }

    public static Component getProgressKeyHint() {
        return Component.translatable("penchant.tooltip.progress.key", Component.keybind(SHOW_PROGRESS_KEYBIND.getName()))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    public static Component getProgressTooltip(EnchantmentProgress progress, Holder<Enchantment> enchantment, int level, ItemStack stack) {
        if (level >= PenchantmentHelper.getMaxLevel(enchantment))
            return Component.literal("  ")
                    .append(FontUtils.getBar(getBarWidth(), getBarWidth()))
                    .append(" ")
                    .append(Component.translatable("penchant.tooltip.progress.max"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE);

        var maxProgress = EnchantmentProgress.getMaxProgress(enchantment, level, stack.getMaxDamage());
        var storedProgress = clamp(progress.getProgress(enchantment), 0, Math.max(maxProgress, 0));
        int filled = maxProgress <= 0 ? 0 : (int) ((long) getBarWidth() * storedProgress / maxProgress);

        return Component.literal("  ")
                .append(FontUtils.getBar(getBarWidth(), filled))
                .append(" ")
                .append(Component.translatable("penchant.tooltip.progress",
                        Component.literal(Integer.toString(storedProgress)).withStyle(ChatFormatting.LIGHT_PURPLE),
                        maxProgress
                ).withStyle(ChatFormatting.DARK_GRAY))
                ;
    }
}
