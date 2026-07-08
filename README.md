# Penchant (NeoForge)

**A rework of enchanting, centered around leveling up enchantments through usage.**

This is an **unofficial NeoForge port** of [Penchant](https://modrinth.com/mod/penchant) by
[ThePotatoArchivist](https://github.com/ThePotatoArchivist/Penchant). It aims for full feature parity with the
original Fabric mod (0.3.11) while using native NeoForge APIs.

| | |
|---|---|
| **Mod ID** | `penchant` |
| **Minecraft** | 26.1.2 |
| **NeoForge** | 26.1.2.78 |
| **Java** | 25 |
| **License** | LGPL-3.0-or-later |

---

## What it does

Instead of gambling levels away at the enchanting table, enchantments start at **level 1** and **level up as you use
the item**. Mine, fight, and take hits, and your gear grows stronger over time.

Core features:

- **Usage-based leveling** - every enchantment on an item gains progress whenever the item takes durability damage
  (mining, attacking, blocking, wearing armor). When progress fills up, the enchantment levels up with a sound and
  particle effect.
- **Reworked enchanting table** - a custom menu that lets you pick which enchantment to apply.
- **Extract enchantments** - pull enchantments back out of gear at a grindstone.
- **Progress tooltips** - hold a key (default: `Left Ctrl`) to see enchantment progress bars on items.
- **Advancements** for building libraries, enchanting, and collecting every enchantment.

### Toggleable modules (built-in datapacks)

Penchant ships several optional behaviors as built-in datapacks you can enable/disable from the
**Data Packs** screen when creating or editing a world:

| Datapack | Enabled by default | Effect |
|---|:---:|---|
| `durability_rework` | ✔ | Reworks Unbreaking/Mending around the new leveling system |
| `bookshelf_placement` | ✔ | More lenient bookshelf placement around the table |
| `table_rework` | ✔ | Enables the custom enchanting table menu + advancements |
| `no_anvil_books` | ✔ | Prevents combining enchanted books on an anvil |
| `loot_rework` | ✔ | Reworks where enchantment books appear in loot & mob equipment |
| `guaranteed_drops` | ✔ | Guarantees certain enchanted drops (e.g. tridents) |
| `reduced_curses` | ✘ | Reduces how often curses appear |

---

## Compatibility with other enchantment mods

**Yes - Penchant is designed to work with mods that add enchantments.** Its mechanics are generic and
tag/data-driven rather than hardcoded to vanilla enchantments:

- **Leveling is universal.** Progress is applied to *every* enchantment present on an item, so enchantments added by
  other mods level up through usage exactly like vanilla ones - no patch required.
- **The enchanting table** forces newly-applied enchantments to level 1 for any enchantment, modded included.
- **Opt-out via tags.** Two enchantment tags control behavior, so a modpack (or the enchantment mod itself) can
  fine-tune integration without touching code:
  - `#penchant:no_leveling` - enchantments in this tag are applied at full level and never gain progress (use this for
    enchantments that don't make sense to level up, e.g. single-level toggles).
  - `#penchant:disabled` - enchantments in this tag are removed from the enchanting table.
  - Single-level enchantments (max level 1) automatically skip the progress bar.
- **Loot integration is additive.** Penchant's `loot_rework` sorts enchantment books into `#penchant:rare`,
  `#penchant:uncommon`, and `#penchant:common` tags. Modded enchantments simply keep their vanilla loot behavior
  unless a datapack adds them to those tags (the port already ships optional entries for a few popular mods).

### Things to keep in mind

- Penchant changes **how enchantments are obtained and leveled**, so a mod that *also* overhauls the enchanting
  table, the anvil, or `EnchantmentHelper` may conflict at the mixin level. In practice, mods that only *add*
  enchantments are fully compatible; mods that *replace enchanting mechanics* may not be.
- Enchantments a mod expects to apply at a high level directly from the table will instead start at level 1 and level
  up over time. If that's undesirable for a specific enchantment, add it to `#penchant:no_leveling`.
- If a modded enchantment behaves oddly, the tags above are the intended, code-free way to adjust it.

---

## Optional integration

- **[Item Descriptions](https://modrinth.com/mod/item-descriptions)** - if installed (client-side), Penchant's
  enchantments show extra descriptive tooltips. It is entirely optional and not bundled.

---

## Building from source

Requires JDK 25.

```bash
# Build the mod jar (output in build/libs/)
./gradlew build

# Run the client / dedicated server in a dev environment
./gradlew runClient
./gradlew runServer

# Regenerate data (tags, enchantments, loot, advancements) into src/generated/resources
./gradlew runData
```

The mixin-heavy port relies on the NeoForge ModDevGradle toolchain; the first build downloads and decompiles the
Minecraft/NeoForge artifacts.

---

## Credits & license

- **Original mod:** [Penchant](https://modrinth.com/mod/penchant) by
  **[ThePotatoArchivist](https://github.com/ThePotatoArchivist/Penchant)**.
- **NeoForge port:** WoXayZ.

Licensed under **LGPL-3.0-or-later**, matching the original mod. See [`LICENSE`](LICENSE).

This is an unofficial port and is not affiliated with or endorsed by the original author.
