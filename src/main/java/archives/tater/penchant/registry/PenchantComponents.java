package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.component.RandomEnchantment;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import static net.minecraft.util.Mth.clamp;

@EventBusSubscriber(modid = Penchant.MOD_ID)
public class PenchantComponents {
    private static <T> DataComponentType<T> buildComponent(Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, boolean cache, boolean ignoreSwapAnimation) {
        var type = DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec);
        if (cache) type.cacheEncoding();
        if (ignoreSwapAnimation) type.ignoreSwapAnimation();
        return type.build();
    }

    public static final DataComponentType<EnchantmentProgress> ENCHANTMENT_PROGRESS = buildComponent(
            EnchantmentProgress.CODEC,
            EnchantmentProgress.STREAM_CODEC,
            true,
            true
    );

    public static final DataComponentType<Integer> ENCHANTMENT_PROGRESS_COST_FACTOR = buildComponent(
            ExtraCodecs.NON_NEGATIVE_INT,
            ByteBufCodecs.INT,
            false,
            false
    );

    public static final DataComponentType<RandomEnchantment> RANDOM_ENCHANTMENT = buildComponent(
            RandomEnchantment.CODEC,
            RandomEnchantment.STREAM_CODEC,
            true,
            false
    );

    public static void register(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, helper -> {
            helper.register(Penchant.id("enchantment_progress"), ENCHANTMENT_PROGRESS);
            helper.register(Penchant.id("enchantment_progress_cost_factor"), ENCHANTMENT_PROGRESS_COST_FACTOR);
            helper.register(Penchant.id("random_enchantment"), RANDOM_ENCHANTMENT);
        });
    }

    @SubscribeEvent
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modifyMatching(
                (item, componentTypes) -> componentTypes.has(DataComponents.MAX_DAMAGE)
                        && !componentTypes.has(ENCHANTMENT_PROGRESS_COST_FACTOR),
                (components, context, item) -> components.set(
                        ENCHANTMENT_PROGRESS_COST_FACTOR,
                        clamp(components.getOrDefault(DataComponents.MAX_DAMAGE, 0) / 100, 1, 8)
                )
        );
    }

    public static void init() {

    }
}
