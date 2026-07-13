package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.component.RandomEnchantment;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.RegisterEvent;

public class PenchantComponents {
    private static <T> DataComponentType<T> buildComponent(Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, boolean cache) {
        var type = DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec);
        if (cache) type.cacheEncoding();
        return type.build();
    }

    public static final DataComponentType<EnchantmentProgress> ENCHANTMENT_PROGRESS = buildComponent(
            EnchantmentProgress.CODEC,
            EnchantmentProgress.STREAM_CODEC,
            true
    );

    public static final DataComponentType<RandomEnchantment> RANDOM_ENCHANTMENT = buildComponent(
            RandomEnchantment.CODEC,
            RandomEnchantment.STREAM_CODEC,
            true
    );

    public static void register(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, helper -> {
            helper.register(Penchant.id("enchantment_progress"), ENCHANTMENT_PROGRESS);
            helper.register(Penchant.id("random_enchantment"), RANDOM_ENCHANTMENT);
        });
    }

    public static void init() {

    }
}
