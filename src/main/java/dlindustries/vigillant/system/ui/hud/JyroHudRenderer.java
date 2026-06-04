package dlindustries.vigillant.system.ui.hud;

import dlindustries.vigillant.system.ui.model.JyroModuleHandle;
import dlindustries.vigillant.system.ui.animation.AnimationMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Monochrome in-game HUD renderer for Jyro-Client.
 *
 * <p>Call {@link #render(DrawContext)} from your HUD render event/mixin. Feed it modules with
 * {@link #setModules(List)} or use {@link #setEntries(List)} for a backend-agnostic placeholder.</p>
 */
public final class JyroHudRenderer {
    private static final int WHITE = 0x00FFFFFF;
    private static final int BLACK = 0x00000000;
    private static final String LOGO = "Jyro-Client";

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final double[] lastFrameTime = new double[1];
    private final Map<String, AnimationMath.AnimatedFloat> slideAnimations = new HashMap<>();
    private final Map<String, AnimationMath.AnimatedFloat> opacityAnimations = new HashMap<>();
    private final List<HudEntry> entries = new ArrayList<>();

    private boolean renderLogo = true;
    private boolean renderArrayList = true;
    private boolean renderBackgrounds = true;
    private boolean renderBorders = true;
    private float hudOpacity = 1.0F;

    /** Connects the HUD to your existing module list through tiny UI handles. */
    public void setModules(List<? extends JyroModuleHandle> modules) {
        entries.clear();
        for (JyroModuleHandle module : modules) {
            entries.add(new HudEntry(module.name(), module.enabled()));
        }
    }

    /** Backend-free hook for placeholders, commands, or custom module wrappers. */
    public void setEntries(List<String> visibleEntries) {
        entries.clear();
        for (String entry : visibleEntries) {
            entries.add(new HudEntry(entry, true));
        }
    }

    public void render(DrawContext context) {
        if (client.textRenderer == null) {
            return;
        }

        float delta = AnimationMath.deltaSeconds(lastFrameTime);
        TextRenderer text = client.textRenderer;

        if (renderLogo) {
            renderLogo(context, text);
        }
        if (renderArrayList) {
            renderArrayList(context, text, delta);
        }
    }

    private void renderLogo(DrawContext context, TextRenderer text) {
        double time = AnimationMath.nowSeconds();
        float breath = 0.72F + 0.28F * (float) ((Math.sin(time * 1.65D) + 1.0D) * 0.5D);
        int color = AnimationMath.alpha(WHITE, breath * hudOpacity);
        int background = AnimationMath.alpha(BLACK, 0.48F * breath * hudOpacity);
        int width = text.getWidth(LOGO) + 10;

        if (renderBackgrounds) {
            context.fill(6, 6, 6 + width, 22, background);
        }
        if (renderBorders) {
            drawBorder(context, 6, 6, width, 16, AnimationMath.alpha(WHITE, 0.85F * breath * hudOpacity));
        }
        context.drawText(text, LOGO, 11, 10, color, false);
    }

    private void renderArrayList(DrawContext context, TextRenderer text, float delta) {
        List<HudEntry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingInt((HudEntry entry) -> text.getWidth(entry.label())).reversed());

        int screenWidth = context.getScaledWindowWidth();
        int y = 8;

        for (HudEntry entry : sorted) {
            int textWidth = text.getWidth(entry.label());
            AnimationMath.AnimatedFloat slide = slideAnimations.computeIfAbsent(
                    entry.label(), key -> new AnimationMath.AnimatedFloat(0.0F, 8.5F));
            AnimationMath.AnimatedFloat opacity = opacityAnimations.computeIfAbsent(
                    entry.label(), key -> new AnimationMath.AnimatedFloat(0.0F, 9.0F));

            slide.target(entry.visible() ? 1.0F : 0.0F).update(delta);
            opacity.target(entry.visible() ? 1.0F : 0.0F).update(delta);

            if (opacity.value() <= 0.01F && !entry.visible()) {
                continue;
            }

            float easedSlide = AnimationMath.easeOutCubic(slide.value());
            int panelWidth = textWidth + 12;
            int offscreenX = screenWidth + 4;
            int targetX = screenWidth - panelWidth - 8;
            int x = Math.round(AnimationMath.lerp(offscreenX, targetX, easedSlide));
            float alpha = opacity.value() * hudOpacity;

            if (renderBackgrounds) {
                context.fill(x, y, x + panelWidth, y + 14, AnimationMath.alpha(BLACK, 0.62F * alpha));
            }
            if (renderBorders) {
                drawBorder(context, x, y, panelWidth, 14, AnimationMath.alpha(WHITE, 0.76F * alpha));
            }
            context.drawText(text, entry.label(), x + 6, y + 3, AnimationMath.alpha(WHITE, alpha), false);
            y += Math.round(AnimationMath.lerp(2.0F, 16.0F, opacity.value()));
        }
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    public void setRenderLogo(boolean renderLogo) {
        this.renderLogo = renderLogo;
    }

    public void setRenderArrayList(boolean renderArrayList) {
        this.renderArrayList = renderArrayList;
    }

    public void setRenderBackgrounds(boolean renderBackgrounds) {
        this.renderBackgrounds = renderBackgrounds;
    }

    public void setRenderBorders(boolean renderBorders) {
        this.renderBorders = renderBorders;
    }

    public void setHudOpacity(float hudOpacity) {
        this.hudOpacity = AnimationMath.saturate(hudOpacity);
    }

    public record HudEntry(String label, boolean visible) {
    }
}
