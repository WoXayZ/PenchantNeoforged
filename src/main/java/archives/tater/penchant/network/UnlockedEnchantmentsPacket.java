package archives.tater.penchant.network;

import archives.tater.penchant.menu.PenchantmentMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public record UnlockedEnchantmentsPacket(Set<Enchantment> unlocked) {
    public static void encode(UnlockedEnchantmentsPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.unlocked.size());
        for (Enchantment enchantment : packet.unlocked) {
            buf.writeVarInt(BuiltInRegistries.ENCHANTMENT.getId(enchantment));
        }
    }

    public static UnlockedEnchantmentsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<Enchantment> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.byId(buf.readVarInt());
            if (enchantment != null) set.add(enchantment);
        }
        return new UnlockedEnchantmentsPacket(set);
    }

    public static void handle(UnlockedEnchantmentsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof PenchantmentMenu menu) {
                menu.setUnlockedFromClient(packet.unlocked);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
