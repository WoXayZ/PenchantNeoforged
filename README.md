# Penchant (Forge 1.20.1)

Unofficial **Forge 1.20.1** port of [Penchant](https://modrinth.com/mod/penchant).

| | |
|---|---|
| **Mod ID** | `penchant` |
| **Port version** | **1.4** |
| **Mod version** | 1.20.1-1.4 |
| **Minecraft** | 1.20.1 |
| **Forge** | 47.4.0 |
| **Java** | 17 |

## Features (1.4)

- Usage-based enchantment leveling (progress stored in NBT)
- Reworked enchanting table menu (pick an enchantment, extract onto books)
- Hold Left Ctrl to view progress tooltips
- No combining enchanted books on the anvil
- Lenient bookshelf placement
- Loot rework (common / uncommon / rare / unique pools; books skip commons & curses)
- Guaranteed enchanted equipment / drowned trident drops

This branch is a **rewrite** for the 1.20.1 enchantment system (not a line-port of NeoForge 1.21+).

## Build

```bash
./gradlew build
```

JAR: `build/libs/penchant-forge-1.20.1-1.4.jar`

## Dev / release QA deps

`runClient` also loads:

- **Patchouli** `1.20.1-85-FORGE` - **required** for the **Tome of Penchant** (craft: Book + Enchanting Table, or Tools & Utilities creative tab)
- **Item Descriptions** `2.5.4+1.20.1` - **optional** client lore in inventory / table tooltips

### CurseForge relations

| Relation | Mod | Why |
|---|---|---|
| **Required** | Patchouli | Tome of Penchant guidebook |
| **Optional** | Item Descriptions | Extra enchantment descriptions |
