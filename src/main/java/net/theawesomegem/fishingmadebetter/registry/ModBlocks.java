package net.theawesomegem.fishingmadebetter.registry;

import net.theawesomegem.fishingmadebetter.Constants;

public final class ModBlocks {
    public static final BlockDefinition BAIT_BOX = new BlockDefinition(Constants.MOD_ID, "baitbox");

    private ModBlocks() {
    }

    public record BlockDefinition(String namespace, String path) {
    }
}
