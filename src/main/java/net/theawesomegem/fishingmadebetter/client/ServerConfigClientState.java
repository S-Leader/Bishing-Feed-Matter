package net.theawesomegem.fishingmadebetter.client;

import net.theawesomegem.fishingmadebetter.common.config.FmbCommonConfig;

/**
 * Client-side snapshot used only by the config GUI.
 */
public final class ServerConfigClientState {
    private static boolean received;
    private static boolean canEdit;
    private static boolean whaleBreaksBlocks = true;
    private static FmbCommonConfig.WhaleDrop whaleDrop = FmbCommonConfig.WhaleDrop.WHALE_STEAK;
    private static int whaleSpawnChancePercent = 20;

    private ServerConfigClientState() {
    }

    public static void update(boolean breaksBlocks, FmbCommonConfig.WhaleDrop drop, int spawnChancePercent, boolean editable) {
        received = true;
        whaleBreaksBlocks = breaksBlocks;
        whaleDrop = drop == null ? FmbCommonConfig.WhaleDrop.WHALE_STEAK : drop;
        whaleSpawnChancePercent = Math.max(0, Math.min(100, spawnChancePercent));
        canEdit = editable;
    }

    public static void clear() {
        received = false;
        canEdit = false;
    }

    public static boolean received() {
        return received;
    }

    public static boolean canEdit() {
        return canEdit;
    }

    public static boolean whaleBreaksBlocks() {
        return received ? whaleBreaksBlocks : FmbCommonConfig.whaleBreaksBlocks();
    }

    public static FmbCommonConfig.WhaleDrop whaleDrop() {
        return received ? whaleDrop : FmbCommonConfig.whaleDrop();
    }

    public static int whaleSpawnChancePercent() {
        return received ? whaleSpawnChancePercent : FmbCommonConfig.whaleSpawnChancePercent();
    }
}
