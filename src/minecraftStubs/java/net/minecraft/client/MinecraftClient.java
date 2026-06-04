package net.minecraft.client;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
public class MinecraftClient {
    public TextRenderer textRenderer = new TextRenderer();
    public Screen currentScreen;
    private static final MinecraftClient INSTANCE = new MinecraftClient();
    public static MinecraftClient getInstance() { return INSTANCE; }
    public void setScreen(Screen screen) { this.currentScreen = screen; }
}
