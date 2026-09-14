# Fishing Evolved — Forge 1.20.1

This branch is a pure Forge port of Fishing Evolved for Minecraft 1.20.1 and Java 17. It keeps the active-reeling and
modular-rod systems while restoring the 1.12.2 Fishing Made Better compatibility model.

## Installation

- Minecraft 1.20.1
- Forge 47.2.30 or newer
- Optional configuration screen: YetAnotherConfigLib 3.4.x
- Optional recipe viewer: JEI 15.x
- Optional integrations: Advanced Fishing: Tides 1.4.x, Aquaculture 2.5.x, and Nether Depths Upgrade 3.x

No Fabric, Fabric API, Architectury, or NeoForge component is required.

## Compatibility changes

- Advanced Fishing: Tides, Aquaculture, and Nether Depths Upgrade fish use editable 1.12.2-style files under
  `config/fishingmadebetter/fishdata`.
- Aquaculture line, bobber, fillet knives, and hook recipes are disabled; its fishing rod recipes remain available.
- Aquaculture's tackle box assembles Fishing Evolved rods using Fishing Evolved hooks, bait, reels, and bobbers.
- Fishing Evolved's treasure and junk rolls reference the live vanilla fishing tables, including datapack and mod
  additions.
- Fish caught by Fishing Evolved carry Aquaculture-compatible weight data for fish mounts.
- JEI displays dynamic filleting, scaling, baiting, container, and rod-attachment recipes.
- Steel and netherite rods, fillet knives, and scaling knives are included.
- Whale, whale steak, and whale burger restore the old compatibility food values and processing path.

## Building

Run `gradlew build`. The Forge jar is written to `build/libs`.

## Credits

- TheAwesomeGem for Fishing Made Better and its 1.12.2 compatibility data.
- SeniorS for the Fishing Evolved codebase used as the porting base.
