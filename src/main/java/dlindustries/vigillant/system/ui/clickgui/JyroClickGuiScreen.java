package dlindustries.vigillant.system.ui.clickgui;

import dlindustries.vigillant.system.ui.model.JyroModuleHandle;
import dlindustries.vigillant.system.ui.animation.AnimationMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sleek monochrome ClickGUI for Jyro-Client.
 *
 * <p>This class owns only presentation and interaction. It calls {@link JyroModuleHandle#toggle()} as
 * the default hook, so it can be dropped onto the existing module backend without changing module logic.</p>
 */
public final class JyroClickGuiScreen extends Screen {
    private static final int WHITE = 0x00FFFFFF;
    private static final int BLACK = 0x00000000;
    private static final int HEADER_HEIGHT = 18;
    private static final int SLOT_HEIGHT = 16;
    private static final int FRAME_WIDTH = 116;
    private static final int FRAME_GAP = 10;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final List<CategoryFrame> frames = new ArrayList<>();
    private final AnimationMath.AnimatedFloat openAnimation = new AnimationMath.AnimatedFloat(0.0F, 7.5F);
    private final double[] lastFrameTime = new double[1];

    private boolean renderBackgroundOverlay = true;
    private boolean renderBorders = true;
    private boolean renderPanelBackgrounds = true;
    private boolean renderHoverInversion = true;
    private boolean renderToggleBars = true;
    private boolean closing;
    private boolean searchFocused;
    private String searchQuery = "";
    private CategoryFrame draggingFrame;
    private int dragOffsetX;
    private int dragOffsetY;

    public JyroClickGuiScreen(List<? extends JyroModuleHandle> modules) {
        super(Text.literal("Jyro-Client"));
        createFrames(modules);
    }

    @Override
    protected void init() {
        openAnimation.target(1.0F);
        closing = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float delta = AnimationMath.deltaSeconds(lastFrameTime);
        openAnimation.target(closing ? 0.0F : 1.0F).update(delta);

        float progress = AnimationMath.jyroEase(openAnimation.value());
        float opacity = progress;
        float scale = AnimationMath.lerp(0.95F, 1.0F, progress);

        if (renderBackgroundOverlay) {
            context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                    AnimationMath.alpha(BLACK, 0.48F * opacity));
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        float centerX = context.getScaledWindowWidth() * 0.5F;
        float centerY = context.getScaledWindowHeight() * 0.5F;
        matrices.translate(centerX, centerY, 0.0F);
        matrices.scale(scale, scale, 1.0F);
        matrices.translate(-centerX, -centerY, 0.0F);

        TextRenderer textRenderer = client.textRenderer;
        renderSearchBar(context, textRenderer, mouseX, mouseY, opacity);
        for (CategoryFrame frame : frames) {
            frame.render(context, textRenderer, mouseX, mouseY, delta, opacity, searchQuery);
        }
        matrices.pop();

        if (closing && openAnimation.isAtTarget()) {
            client.setScreen(null);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (isSearchHovered(mouseX, mouseY)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        for (int i = frames.size() - 1; i >= 0; i--) {
            CategoryFrame frame = frames.get(i);
            if (frame.isHeaderHovered(mouseX, mouseY)) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    frame.collapsed = !frame.collapsed;
                    return true;
                }

                draggingFrame = frame;
                dragOffsetX = (int) mouseX - frame.x;
                dragOffsetY = (int) mouseY - frame.y;
                frames.remove(i);
                frames.add(frame);
                return true;
            }

            ModuleSlot slot = frame.slotAt(mouseX, mouseY, searchQuery);
            if (slot != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                slot.module.toggle();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingFrame != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            draggingFrame.x = Math.round((float) mouseX) - dragOffsetX;
            draggingFrame.y = Math.round((float) mouseY) - dragOffsetY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingFrame = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchFocused || !searchQuery.isEmpty()) {
                searchFocused = false;
                searchQuery = "";
                return true;
            }
            beginClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_F && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            searchFocused = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && !Character.isISOControl(chr) && searchQuery.length() < 32) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        beginClose();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }


    private void renderSearchBar(DrawContext context, TextRenderer text, int mouseX, int mouseY, float opacity) {
        int x = Math.max(22, (context.getScaledWindowWidth() - 180) / 2);
        int y = 10;
        int width = 180;
        int height = 16;
        boolean hovered = isSearchHovered(mouseX, mouseY);
        context.fill(x, y, x + width, y + height, AnimationMath.alpha(BLACK, 0.78F * opacity));
        drawBorder(context, x, y, width, height, AnimationMath.alpha(WHITE, (searchFocused || hovered ? 1.0F : 0.62F) * opacity));
        String label = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        float textOpacity = searchQuery.isEmpty() ? 0.54F : 1.0F;
        context.drawText(text, label, x + 6, y + 4, AnimationMath.alpha(WHITE, textOpacity * opacity), false);
        if (searchFocused && ((int) (AnimationMath.nowSeconds() * 2.0D) & 1) == 0) {
            int cursorX = x + 7 + text.getWidth(label);
            context.fill(cursorX, y + 4, cursorX + 1, y + 12, AnimationMath.alpha(WHITE, opacity));
        }
    }

    private boolean isSearchHovered(double mouseX, double mouseY) {
        int x = Math.max(22, (width - 180) / 2);
        return mouseX >= x && mouseX <= x + 180 && mouseY >= 10 && mouseY <= 26;
    }

    private void beginClose() {
        closing = true;
        openAnimation.target(0.0F);
    }

    private void createFrames(List<? extends JyroModuleHandle> modules) {
        Map<String, List<JyroModuleHandle>> grouped = new LinkedHashMap<>();
        for (JyroModuleHandle module : modules) {
            grouped.computeIfAbsent(displayName(module.category()), ignored -> new ArrayList<>()).add(module);
        }

        String[] preferred = {"Mace", "Crystal", "Sword", "Render"};
        int x = 22;
        for (String category : preferred) {
            List<JyroModuleHandle> categoryModules = grouped.getOrDefault(category, List.of());
            frames.add(new CategoryFrame(category, x, 36, categoryModules));
            x += FRAME_WIDTH + FRAME_GAP;
        }

        for (Map.Entry<String, List<JyroModuleHandle>> entry : grouped.entrySet()) {
            if (contains(preferred, entry.getKey())) {
                continue;
            }
            frames.add(new CategoryFrame(entry.getKey(), x, 36, entry.getValue()));
            x += FRAME_WIDTH + FRAME_GAP;
        }
    }

    private static boolean contains(String[] categories, String category) {
        for (String value : categories) {
            if (value.equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }

    private static String displayName(String rawCategory) {
        String raw = rawCategory == null ? "Client" : rawCategory.trim();
        if (raw.equalsIgnoreCase("mace") || raw.equalsIgnoreCase("spearmace")) {
            return "Mace";
        }
        if (raw.equalsIgnoreCase("crystal")) {
            return "Crystal";
        }
        if (raw.equalsIgnoreCase("sword")) {
            return "Sword";
        }
        if (raw.equalsIgnoreCase("render")) {
            return "Render";
        }
        if (raw.isBlank()) {
            return "Client";
        }
        return raw.substring(0, 1).toUpperCase(Locale.ROOT) + raw.substring(1);
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    public void setRenderBackgroundOverlay(boolean renderBackgroundOverlay) {
        this.renderBackgroundOverlay = renderBackgroundOverlay;
    }

    public void setRenderBorders(boolean renderBorders) {
        this.renderBorders = renderBorders;
    }

    public void setRenderPanelBackgrounds(boolean renderPanelBackgrounds) {
        this.renderPanelBackgrounds = renderPanelBackgrounds;
    }

    public void setRenderHoverInversion(boolean renderHoverInversion) {
        this.renderHoverInversion = renderHoverInversion;
    }

    public void setRenderToggleBars(boolean renderToggleBars) {
        this.renderToggleBars = renderToggleBars;
    }

    private final class CategoryFrame {
        private final String title;
        private final List<ModuleSlot> slots;
        private final AnimationMath.AnimatedFloat hoverAnimation = new AnimationMath.AnimatedFloat(0.0F, 11.0F);
        private final AnimationMath.AnimatedFloat collapseAnimation = new AnimationMath.AnimatedFloat(1.0F, 9.0F);
        private int x;
        private int y;
        private boolean collapsed;

        private CategoryFrame(String title, int x, int y, List<JyroModuleHandle> modules) {
            this.title = title;
            this.x = x;
            this.y = y;
            this.slots = new ArrayList<>();
            modules.stream()
                    .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                    .map(ModuleSlot::new)
                    .forEach(slots::add);
        }

        private void render(DrawContext context, TextRenderer text, int mouseX, int mouseY, float delta, float opacity, String filter) {
            boolean headerHovered = isHeaderHovered(mouseX, mouseY);
            float headerHover = hoverAnimation.target(headerHovered ? 1.0F : 0.0F).update(delta);
            float expanded = collapseAnimation.target(collapsed ? 0.0F : 1.0F).update(delta);
            List<ModuleSlot> visibleSlotsList = visibleSlots(filter);
            int totalHeight = HEADER_HEIGHT + Math.round(visibleSlotsList.size() * SLOT_HEIGHT * expanded);

            if (renderPanelBackgrounds) {
                context.fill(x, y, x + FRAME_WIDTH, y + totalHeight, AnimationMath.alpha(BLACK, 0.82F * opacity));
            }
            if (renderHoverInversion && headerHover > 0.01F) {
                context.fill(x, y, x + FRAME_WIDTH, y + HEADER_HEIGHT,
                        AnimationMath.alpha(WHITE, headerHover * 0.92F * opacity));
            }
            if (renderBorders) {
                drawBorder(context, x, y, FRAME_WIDTH, totalHeight, AnimationMath.alpha(WHITE, 0.90F * opacity));
            }

            int titleColor = renderHoverInversion && headerHover > 0.55F
                    ? AnimationMath.alpha(BLACK, opacity)
                    : AnimationMath.alpha(WHITE, opacity);
            context.drawText(text, title, x + 7, y + 5, titleColor, false);

            if (expanded <= 0.01F) {
                return;
            }

            int visibleSlots = Math.max(0, Math.min(visibleSlotsList.size(), Math.round(visibleSlotsList.size() * expanded + 0.001F)));
            int slotY = y + HEADER_HEIGHT;
            for (int i = 0; i < visibleSlots; i++) {
                visibleSlotsList.get(i).render(context, text, x, slotY, mouseX, mouseY, delta, opacity * expanded);
                slotY += SLOT_HEIGHT;
            }
        }

        private boolean isHeaderHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + FRAME_WIDTH && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
        }

        private ModuleSlot slotAt(double mouseX, double mouseY, String filter) {
            if (collapsed || mouseX < x || mouseX > x + FRAME_WIDTH) {
                return null;
            }
            int relativeY = (int) mouseY - y - HEADER_HEIGHT;
            if (relativeY < 0) {
                return null;
            }
            List<ModuleSlot> visibleSlotsList = visibleSlots(filter);
            int index = relativeY / SLOT_HEIGHT;
            return index >= 0 && index < visibleSlotsList.size() ? visibleSlotsList.get(index) : null;
        }


        private List<ModuleSlot> visibleSlots(String filter) {
            if (filter == null || filter.isBlank()) {
                return slots;
            }
            String needle = filter.toLowerCase(Locale.ROOT);
            return slots.stream()
                    .filter(slot -> slot.module.name().toLowerCase(Locale.ROOT).contains(needle))
                    .toList();
        }
    }

    private final class ModuleSlot {
        private final JyroModuleHandle module;
        private final AnimationMath.AnimatedFloat hoverAnimation = new AnimationMath.AnimatedFloat(0.0F, 12.0F);
        private final AnimationMath.AnimatedFloat toggleAnimation = new AnimationMath.AnimatedFloat(0.0F, 8.0F);
        private final Map<String, AnimationMath.AnimatedFloat> settingAnimations = new HashMap<>();

        private ModuleSlot(JyroModuleHandle module) {
            this.module = module;
            this.toggleAnimation.snap(module.enabled() ? 1.0F : 0.0F);
        }

        private void render(DrawContext context, TextRenderer text, int x, int y, int mouseX, int mouseY, float delta, float opacity) {
            boolean hovered = mouseX >= x && mouseX <= x + FRAME_WIDTH && mouseY >= y && mouseY <= y + SLOT_HEIGHT;
            float hover = hoverAnimation.target(hovered ? 1.0F : 0.0F).update(delta);
            float enabled = toggleAnimation.target(module.enabled() ? 1.0F : 0.0F).update(delta);

            if (renderHoverInversion && hover > 0.01F) {
                context.fill(x + 1, y, x + FRAME_WIDTH - 1, y + SLOT_HEIGHT,
                        AnimationMath.alpha(WHITE, hover * 0.78F * opacity));
            }

            if (renderToggleBars) {
                int barWidth = Math.round((FRAME_WIDTH - 2) * AnimationMath.easeOutCubic(enabled));
                if (barWidth > 0) {
                    context.fill(x + 1, y + SLOT_HEIGHT - 2, x + 1 + barWidth, y + SLOT_HEIGHT - 1,
                            AnimationMath.alpha(WHITE, opacity));
                }
            }

            int textColor = renderHoverInversion && hover > 0.52F
                    ? AnimationMath.alpha(BLACK, opacity)
                    : AnimationMath.alpha(WHITE, opacity);
            context.drawText(text, module.name(), x + 7, y + 4, textColor, false);

            // Hook point for future per-setting visuals: callers can populate this map by setting name.
            settingAnimations.entrySet().removeIf(entry -> entry.getValue().value() <= 0.0F && entry.getValue().target() <= 0.0F);
        }
    }
}
