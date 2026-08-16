package archives.tater.penchant.network;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.menu.PenchantmentMenu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.Set;

public final class PenchantNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Penchant.id("main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id;

    private PenchantNetworking() {}

    public static void register() {
        CHANNEL.registerMessage(id++, SelectEnchantmentPacket.class,
                SelectEnchantmentPacket::encode,
                SelectEnchantmentPacket::decode,
                SelectEnchantmentPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, UnlockedEnchantmentsPacket.class,
                UnlockedEnchantmentsPacket::encode,
                UnlockedEnchantmentsPacket::decode,
                UnlockedEnchantmentsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendUnlocked(ServerPlayer player, Set<Enchantment> unlocked) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UnlockedEnchantmentsPacket(unlocked));
    }
}
