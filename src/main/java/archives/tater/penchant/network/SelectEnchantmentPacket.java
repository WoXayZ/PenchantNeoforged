package archives.tater.penchant.network;

import archives.tater.penchant.menu.PenchantmentMenu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectEnchantmentPacket(int enchantmentId) {
    public static void encode(SelectEnchantmentPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.enchantmentId);
    }

    public static SelectEnchantmentPacket decode(FriendlyByteBuf buf) {
        return new SelectEnchantmentPacket(buf.readVarInt());
    }

    public static void handle(SelectEnchantmentPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof PenchantmentMenu menu)) return;
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.byId(packet.enchantmentId);
            if (enchantment != null) menu.handleEnchant(enchantment);
        });
        ctx.get().setPacketHandled(true);
    }
}
