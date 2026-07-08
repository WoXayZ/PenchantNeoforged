package archives.tater.penchant.network;

import archives.tater.penchant.Penchant;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.enchantment.Enchantment;

public record EnchantPayload(Holder<Enchantment> enchantment) implements CustomPacketPayload {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final Type<EnchantPayload> TYPE = new Type<>(Penchant.id("enchant"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantPayload> CODEC = ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT)
            .map(EnchantPayload::new, EnchantPayload::enchantment);
}
