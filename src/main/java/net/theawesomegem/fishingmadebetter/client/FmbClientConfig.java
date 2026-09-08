package net.theawesomegem.fishingmadebetter.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.theawesomegem.fishingmadebetter.Constants;

public final class FmbClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ClientConfig INSTANCE = new ClientConfig();
    private static Path configPath;

    private FmbClientConfig() {
    }

    public static ClientConfig get() {
        return INSTANCE;
    }

    public static void init(Path configDirectory) {
        configPath = configDirectory.resolve(Constants.MOD_ID + "-client.json");
        load();
    }

    public static void load() {
        if (configPath == null || !Files.exists(configPath)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            ClientConfig loaded = GSON.fromJson(reader, ClientConfig.class);
            if (loaded != null) {
                INSTANCE.hudAnchor = loaded.hudAnchor == null ? HudAnchor.TOP_CENTER : loaded.hudAnchor;
                INSTANCE.hudOffsetX = loaded.hudOffsetX;
                INSTANCE.hudOffsetY = loaded.hudOffsetY;
                INSTANCE.showHudDistance = loaded.showHudDistance;
            }
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to load Fishing Evolved client config", exception);
        }
        INSTANCE.clamp();
    }

    public static void save() {
        if (configPath == null) {
            return;
        }

        INSTANCE.clamp();
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to save Fishing Evolved client config", exception);
        }
    }

    public static final class ClientConfig {
        public HudAnchor hudAnchor = HudAnchor.TOP_CENTER;
        public int hudOffsetX;
        public int hudOffsetY = 4;
        public boolean showHudDistance = true;

        private void clamp() {
            hudOffsetX = Math.max(-500, Math.min(500, hudOffsetX));
            hudOffsetY = Math.max(-300, Math.min(300, hudOffsetY));
        }
    }

    public enum HudAnchor {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }
}
