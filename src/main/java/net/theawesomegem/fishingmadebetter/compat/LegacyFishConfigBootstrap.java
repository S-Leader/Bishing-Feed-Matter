package net.theawesomegem.fishingmadebetter.compat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.theawesomegem.fishingmadebetter.Constants;

public final class LegacyFishConfigBootstrap {
    private static final String RESOURCE_ROOT = "/data/fishingmadebetter/fishingmadebetter/fish/";

    private LegacyFishConfigBootstrap() {
    }

    public static Path installDefaults(Path forgeConfigDirectory) {
        Path fishDataDirectory = forgeConfigDirectory.resolve(Constants.MOD_ID).resolve("fishdata");
        try {
            Files.createDirectories(fishDataDirectory);
            copyIfMissing(fishDataDirectory, "aquaculture.json");
            copyIfMissing(fishDataDirectory, "netherdepths.json");
        } catch (IOException exception) {
            Constants.LOG.warn("Unable to install legacy-style fish configuration files: {}", exception.getMessage());
        }
        return fishDataDirectory;
    }

    private static void copyIfMissing(Path directory, String fileName) throws IOException {
        Path target = directory.resolve(fileName);
        if (Files.exists(target)) {
            return;
        }
        try (InputStream stream = LegacyFishConfigBootstrap.class.getResourceAsStream(RESOURCE_ROOT + fileName)) {
            if (stream == null) {
                throw new IOException("Missing bundled configuration " + fileName);
            }
            Files.copy(stream, target);
        }
    }
}
