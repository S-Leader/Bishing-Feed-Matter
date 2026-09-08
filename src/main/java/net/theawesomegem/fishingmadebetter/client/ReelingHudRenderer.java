package net.theawesomegem.fishingmadebetter.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FishingHook;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.FmbFishingHook;

/**
 * Forge 1.20.1 rendering of the original 1.12.2 GuiReelingOverlay.
 *
 * The positions, layer order, texture coordinate sizes and default anchoring
 * intentionally mirror the 1.12.2 HUD rather than using a redesigned layout.
 */
public final class ReelingHudRenderer {
    private static final ResourceLocation OUTLINE = new ResourceLocation(Constants.MOD_ID, "textures/gui/reeling_hud_outline.png");
    private static final ResourceLocation TARGET = new ResourceLocation(Constants.MOD_ID, "textures/gui/reeling_hud_target.png");
    private static final ResourceLocation POSITION = new ResourceLocation(Constants.MOD_ID, "textures/gui/reeling_hud_position.png");
    private static final ResourceLocation UNDERLAY = new ResourceLocation(Constants.MOD_ID, "textures/gui/reeling_hud_underoverlay.png");
    private static final ResourceLocation FULLSIZE = new ResourceLocation(Constants.MOD_ID, "textures/gui/reeling_hud_fullsize.png");
    private static final ResourceLocation BIOME = new ResourceLocation(Constants.MOD_ID, "textures/gui/reeling_hud_biome.png");

    private static final int BACKGROUND_BAR_WIDTH = 128;
    private static final int BACKGROUND_BAR_HEIGHT = 24;
    private static final int OUTLINE_BAR_WIDTH = BACKGROUND_BAR_WIDTH + 6;
    private static final int OUTLINE_BAR_HEIGHT = BACKGROUND_BAR_HEIGHT + 6;
    private static final int GUI_TEXTURE_SIZE = 256;

    private ReelingHudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isAlive()) {
            return;
        }

        FishingHook hook = minecraft.player.fishing;
        if (!(hook instanceof FmbFishingHook fmbHook)) {
            return;
        }

        // 1.12.2 only displayed GuiReelingOverlay once a fish had actually bitten.
        // fishDepth is zero while merely waiting for a bite and is initialized when
        // the minigame starts, so it is the synchronized equivalent of isFishing().
        if (fmbHook.getHudFishDepth() <= 0) {
            return;
        }

        int baseX = getBarPosX(graphics.guiWidth());
        int baseY = getBarPosY(graphics.guiHeight());

        // Keep the original 1.12.2 quirk: the distance label is anchored to the
        // un-offset meter position, while the meter itself receives X/Y offsets.
        if (FmbClientConfig.get().showHudDistance) {
            double distance = (double) (fmbHook.getHudFishDepth() - fmbHook.getHudFishDistance()) / 10.0D;
            String distanceText = Component.translatable("fishingmadebetter.reelingoverlay.distance").getString()
                    + String.format(": %sm", distance);
            graphics.drawString(
                    minecraft.font,
                    distanceText,
                    baseX + (OUTLINE_BAR_WIDTH / 2) - 30,
                    baseY + OUTLINE_BAR_HEIGHT + 2,
                    lineColor(fmbHook.getHudLineBreak()),
                    true
            );
        }

        int x = baseX + FmbClientConfig.get().hudOffsetX;
        int y = baseY + FmbClientConfig.get().hudOffsetY;

        RenderSystem.enableBlend();

        // drawTexturedModalRect in 1.12.2 always used a 256x256 texture sheet.
        graphics.blit(OUTLINE, x - 3, y - 3, 0, 0,
                OUTLINE_BAR_WIDTH, OUTLINE_BAR_HEIGHT, GUI_TEXTURE_SIZE, GUI_TEXTURE_SIZE);

        renderBackground(graphics, fmbHook, x, y);
        renderTarget(graphics, fmbHook, x, y);
        renderPosition(graphics, fmbHook, x, y);

        RenderSystem.disableBlend();
    }

    private static void renderBackground(GuiGraphics graphics, FmbFishingHook hook, int x, int y) {
        int dimension = hook.getHudBackgroundDimension();
        int cave = hook.getHudBackgroundCave();
        int time = hook.getHudBackgroundTime();
        int liquid = hook.getHudBackgroundLiquid();
        int biome = hook.getHudBackgroundBiome();

        if (dimension == 1) {
            blitBackground(graphics, UNDERLAY, x, y, 0, liquid == 0 ? 7 : liquid);
            blitBackground(graphics, FULLSIZE, x, y, 0, 0);
        } else if (dimension == -1) {
            blitBackground(graphics, UNDERLAY, x, y, 0, liquid);
            blitBackground(graphics, FULLSIZE, x, y, 0, 1);
        } else if (cave == 1) {
            blitBackground(graphics, UNDERLAY, x, y, 0, liquid == 0 ? 9 : liquid);
            blitBackground(graphics, FULLSIZE, x, y, 0, 2);
        } else {
            if (liquid == 0) {
                blitBackground(graphics, UNDERLAY, x, y, 1, biome);
            } else {
                blitBackground(graphics, UNDERLAY, x, y, 0, liquid);
            }

            blitBackground(graphics, UNDERLAY, x, y, 0, time + 3);
            blitBackground(graphics, BIOME, x, y, 0, biome);
            blitBackground(graphics, UNDERLAY, x, y, 0, time + 5);
        }
    }

    private static void blitBackground(GuiGraphics graphics, ResourceLocation texture,
                                       int x, int y, int indexX, int indexY) {
        graphics.blit(
                texture,
                x,
                y,
                BACKGROUND_BAR_WIDTH * indexX,
                BACKGROUND_BAR_HEIGHT * indexY,
                BACKGROUND_BAR_WIDTH,
                BACKGROUND_BAR_HEIGHT,
                GUI_TEXTURE_SIZE,
                GUI_TEXTURE_SIZE
        );
    }

    private static void renderTarget(GuiGraphics graphics, FmbFishingHook hook, int x, int y) {
        int reelTarget = hook.getHudReelTarget() / 10;
        // Mirrors drawModalRectWithCustomSizedTexture(..., 6, 6, 6, 6).
        graphics.blit(TARGET, x + 16 + reelTarget - 3, y + 14,
                0, 0, 6, 6, 6, 6);
    }

    private static void renderPosition(GuiGraphics graphics, FmbFishingHook hook, int x, int y) {
        int reelAmount = hook.getHudReelAmount() / 10;
        int variance = hook.getHudErrorVariance() / 10;
        int width = variance * 2;
        int left = x + 16 + reelAmount - variance;

        // Left cap: drawModalRectWithCustomSizedTexture(..., 0, 0, 2, 8, 8, 8).
        graphics.blit(POSITION, left, y + 13, 0, 0, 2, 8, 8, 8);

        // Middle: drawScaledCustomSizeModalRect(..., 2, 0, 4, 8,
        // width - 4, 8, 8, 8). This stretches only the 4px middle slice.
        if (width - 4 > 0) {
            graphics.blit(POSITION, left + 2, y + 13, width - 4, 8,
                    2.0F, 0.0F, 4, 8, 8, 8);
        }

        // Right cap: drawModalRectWithCustomSizedTexture(..., 6, 0, 2, 8, 8, 8).
        graphics.blit(POSITION, x + 16 + reelAmount + variance - 2, y + 13,
                6, 0, 2, 8, 8, 8);
    }

    private static int lineColor(int input) {
        int red = (int) Math.min(255, (double) input * 8.5D);
        int green = (int) Math.min(255, 510.0D - (double) input * 8.5D);

        red = (red << 16) & 0x00FF0000;
        green = (green << 8) & 0x0000FF00;

        return 0xFF000000 | red | green;
    }

    private static int getBarPosX(int scaledWidth) {
        return switch (FmbClientConfig.get().hudAnchor) {
            case TOP_LEFT, BOTTOM_LEFT -> 0;
            case TOP_RIGHT, BOTTOM_RIGHT -> scaledWidth - BACKGROUND_BAR_WIDTH;
            case TOP_CENTER, BOTTOM_CENTER -> (scaledWidth / 2) - (BACKGROUND_BAR_WIDTH / 2);
        };
    }

    private static int getBarPosY(int scaledHeight) {
        return switch (FmbClientConfig.get().hudAnchor) {
            case TOP_LEFT, TOP_RIGHT, TOP_CENTER -> 0;
            case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER -> scaledHeight - BACKGROUND_BAR_HEIGHT;
        };
    }
}
