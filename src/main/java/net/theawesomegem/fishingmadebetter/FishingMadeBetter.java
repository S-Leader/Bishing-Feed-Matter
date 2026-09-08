package net.theawesomegem.fishingmadebetter;

import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;

public final class FishingMadeBetter {
    private FishingMadeBetter() {
    }

    public static void init() {
        Constants.LOG.info("Initializing {}", Constants.MOD_NAME);
        FishDataRegistry.init();
    }
}
