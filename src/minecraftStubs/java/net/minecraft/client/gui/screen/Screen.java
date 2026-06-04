package net.minecraft.client.gui.screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
public class Screen {
    protected int width = 854;
    protected int height = 480;
    protected Screen(Text title) {}
    protected void init() {}
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {}
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean charTyped(char chr, int modifiers) { return false; }
    public void close() {}
    public boolean shouldPause() { return true; }
}
