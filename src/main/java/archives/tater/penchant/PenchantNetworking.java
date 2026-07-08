package archives.tater.penchant;

import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.network.EnchantPayload;
import archives.tater.penchant.network.UnlockedEnchantmentsPayload;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles registration and dispatch of Penchant's network payloads.
 * <p>
 * Payload records and stream codecs are vanilla {@link net.minecraft.network.protocol.common.custom.CustomPacketPayload}s,
 * so only the registration and sending glue differs from the Fabric original.
 */
public final class PenchantNetworking {
    public static final String VERSION = "1";

    private PenchantNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(UnlockedEnchantmentsPayload.TYPE, UnlockedEnchantmentsPayload.CODEC, PenchantNetworking::handleUnlocked);
        registrar.playToServer(EnchantPayload.TYPE, EnchantPayload.CODEC, PenchantNetworking::handleEnchant);
    }

    private static void handleEnchant(EnchantPayload payload, IPayloadContext context) {
        if (!(context.player().containerMenu instanceof PenchantmentMenu menu)) {
            Penchant.LOGGER.warn("Received enchant payload but enchantment menu was not open");
            return;
        }
        menu.handleEnchant(payload.enchantment());
    }

    private static void handleUnlocked(UnlockedEnchantmentsPayload payload, IPayloadContext context) {
        if (!(context.player().containerMenu instanceof PenchantmentMenu menu)) {
            Penchant.LOGGER.warn("Received enchantments payload but enchantment menu was not open");
            return;
        }
        menu.setUnlockedEnchantments(payload.unlocked());
    }
}
