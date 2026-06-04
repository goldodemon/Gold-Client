package net.minecraft.client.gui;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
public class DrawContext {
    public void fill(int x1, int y1, int x2, int y2, int color) {}
    public void drawText(TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {}
    public int getScaledWindowWidth() { return 854; }
    public int getScaledWindowHeight() { return 480; }
    public MatrixStack getMatrices() { return new MatrixStack(); }
}
