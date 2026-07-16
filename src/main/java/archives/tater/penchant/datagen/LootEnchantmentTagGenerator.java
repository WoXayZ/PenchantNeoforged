package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LootEnchantmentTagGenerator extends PenchantTagsProvider<Enchantment> {

    public static final List<ResourceKey<Enchantment>> UNIQUE = List.of(
            Enchantments.WIND_BURST, // ominous vault
            Enchantments.SOUL_SPEED, // bartering/bastion
            Enchantments.SWIFT_SNEAK // ancient city
    );

    public static final List<ResourceKey<Enchantment>> RARE = List.of(
            Enchantments.FROST_WALKER, // igloo
            Enchantments.FIRE_ASPECT, // nether fortress, ruined portal
            Enchantments.FLAME, // nether fortress, ruined portal
            Enchantments.SILK_TOUCH, // mineshaft, dungeon
            Enchantments.FORTUNE, // mineshaft, dungeon
            Enchantments.RESPIRATION, // ocean ruins, shipwreck, buried treasure
            Enchantments.DEPTH_STRIDER, // ocean ruins, shipwreck, buried treasure
            Enchantments.CHANNELING, // ruins, buried treasure
            Enchantments.RIPTIDE, // ruins, buried treasure
            Enchantments.THORNS, // desert temple
            Enchantments.INFINITY, // jungle temple
            Enchantments.MULTISHOT // pillager outpost
    );

    public static final List<ResourceKey<Enchantment>> UNCOMMON = List.of(
            Enchantments.AQUA_AFFINITY,
            Enchantments.FEATHER_FALLING,
            Enchantments.FIRE_PROTECTION,
            Enchantments.BLAST_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.SMITE,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.SWEEPING_EDGE,
            Enchantments.KNOCKBACK,
            Enchantments.PUNCH,
            Enchantments.LOOTING,
            Enchantments.LUCK_OF_THE_SEA
    );

    public static final List<ResourceKey<Enchantment>> COMMON = List.of(
            Enchantments.EFFICIENCY,
            Enchantments.PROTECTION,
            Enchantments.SHARPNESS,
            Enchantments.UNBREAKING,
            Enchantments.POWER,
            Enchantments.PIERCING,
            Enchantments.QUICK_CHARGE,
            Enchantments.IMPALING,
            Enchantments.LOYALTY,
            Enchantments.LURE
    );

    private static ResourceKey<Enchantment> createOptionalId(String namespace, String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(namespace, path));
    }

    public LootEnchantmentTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENCHANTMENT, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(PenchantEnchantmentTags.UNIQUE)
                .addAll(UNIQUE);

        builder(PenchantEnchantmentTags.RARE)
                .addAll(RARE)
                .addOptional(createOptionalId("veinminer-enchantment", "veinminer"))
                .addOptional(createOptionalId("veinminer_enchantment", "veinminer"));

        builder(PenchantEnchantmentTags.UNCOMMON)
                .addAll(UNCOMMON)
                .addOptional(createOptionalId("farmersdelight", "backstabbing"));

        builder(PenchantEnchantmentTags.COMMON)
                .addAll(COMMON);

        builder(EnchantmentTags.TREASURE)
                .addTag(PenchantEnchantmentTags.RARE)
                .addTag(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.NON_TREASURE)
                .remove(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .remove(PenchantEnchantmentTags.UNCOMMON)
                .remove(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.TRADEABLE)
                .remove(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .remove(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.ON_RANDOM_LOOT)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .addTag(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .addTag(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
    }
}
