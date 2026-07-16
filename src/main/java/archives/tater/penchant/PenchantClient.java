package archives.tater.penchant;

import archives.tater.penchant.client.FontUtils;
import archives.tater.penchant.client.KeyMappingExt;
import archives.tater.penchant.client.PenchantClientConfig;
import archives.tater.penchant.client.gui.screen.PenchantmentScreen;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.registry.PenchantMenus;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.Util.makeDescriptionId;

@Mod(value = Penchant.MOD_ID, dist = Dist.CLIENT)
public class PenchantClient {
    public static final KeyMapping.Category PENCHANT_CATEGORY = KeyMapping.Category.register(Penchant.id("keybinds"));

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
        if (level >= enchantment.value().getMaxLevel())
            return Component.literal("  ")
                    .append(FontUtils.getBar(getBarWidth(), getBarWidth()))
                    .append(" ")
                    .append(Component.translatable("penchant.tooltip.progress.max"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE);

        var maxProgress = EnchantmentProgress.getMaxProgress(enchantment, level, stack.getMaxDamage());

        return Component.literal("  ")
                .append(FontUtils.getBar(getBarWidth(), getBarWidth() * progress.getProgress(enchantment) / maxProgress))
                .append(" ")
                .append(Component.translatable("penchant.tooltip.progress",
                        Component.literal(Integer.toString(progress.getProgress(enchantment))).withStyle(ChatFormatting.LIGHT_PURPLE),
                        maxProgress
                ).withStyle(ChatFormatting.DARK_GRAY))
                ;
    }
}
