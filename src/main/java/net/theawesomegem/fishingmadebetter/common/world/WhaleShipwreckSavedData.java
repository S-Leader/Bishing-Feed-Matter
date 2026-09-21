package net.theawesomegem.fishingmadebetter.common.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

final class WhaleShipwreckSavedData extends SavedData {
    private static final String DATA_NAME = "fishingmadebetter_whale_shipwrecks";
    private final LongSet checkedShipwrecks = new LongOpenHashSet();

    static WhaleShipwreckSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                WhaleShipwreckSavedData::load,
                WhaleShipwreckSavedData::new,
                DATA_NAME
        );
    }

    private static WhaleShipwreckSavedData load(CompoundTag tag) {
        WhaleShipwreckSavedData data = new WhaleShipwreckSavedData();
        data.checkedShipwrecks.addAll(LongOpenHashSet.of(tag.getLongArray("CheckedShipwrecks")));
        return data;
    }

    boolean hasChecked(long shipwreckChunk) {
        return checkedShipwrecks.contains(shipwreckChunk);
    }

    void markChecked(long shipwreckChunk) {
        if (checkedShipwrecks.add(shipwreckChunk)) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("CheckedShipwrecks", checkedShipwrecks.toLongArray());
        return tag;
    }
}
