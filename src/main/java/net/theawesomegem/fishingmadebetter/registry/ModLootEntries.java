package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.loot.OptionalModLootTableEntry;

public final class ModLootEntries {
    public static final DeferredRegister<LootPoolEntryType> LOOT_ENTRIES =
            DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, Constants.MOD_ID);

    public static final RegistryObject<LootPoolEntryType> OPTIONAL_MOD_LOOT_TABLE = LOOT_ENTRIES.register(
            "optional_mod_loot_table",
            () -> new LootPoolEntryType(new OptionalModLootTableEntry.Serializer())
    );

    private ModLootEntries() {
    }
}
