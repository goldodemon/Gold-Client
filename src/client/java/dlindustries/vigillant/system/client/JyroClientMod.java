package dlindustries.vigillant.system.client;

import dlindustries.vigillant.system.ui.clickgui.JyroClickGuiScreen;
import dlindustries.vigillant.system.ui.hud.JyroHudRenderer;
import dlindustries.vigillant.system.ui.model.JyroModuleHandle;
import dlindustries.vigillant.system.ui.model.SimpleJyroModuleHandle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal standalone client entrypoint so Gradle produces a usable Fabric jar.
 * Replace {@link #modules} with adapters to your real ModuleManager when integrating.
 */
public final class JyroClientMod implements ClientModInitializer {
    public static final String MOD_ID = "jyro-client";

    private static final JyroHudRenderer HUD_RENDERER = new JyroHudRenderer();
    private static final List<JyroModuleHandle> modules = new ArrayList<>();
    private static KeyBinding openClickGuiKey;

    @Override
    public void onInitializeClient() {
        seedDemoModules();
        HUD_RENDERER.setModules(modules);

        openClickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jyro-client.open_click_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.jyro-client.ui"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openClickGuiKey.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new JyroClickGuiScreen(modules));
            }
        });
    }

    public static JyroHudRenderer hudRenderer() {
        return HUD_RENDERER;
    }

    public static List<JyroModuleHandle> modules() {
        return modules;
    }

    private static void seedDemoModules() {
        if (!modules.isEmpty()) {
            return;
        }

        modules.add(new SimpleJyroModuleHandle("MaceSwap", "Mace", true));
        modules.add(new SimpleJyroModuleHandle("DiveBomber", "Mace", false));
        modules.add(new SimpleJyroModuleHandle("PearlCatch", "Mace", false));
        modules.add(new SimpleJyroModuleHandle("AutoCrystal", "Crystal", true));
        modules.add(new SimpleJyroModuleHandle("AnchorMacro", "Crystal", false));
        modules.add(new SimpleJyroModuleHandle("TotemOffhand", "Crystal", true));
        modules.add(new SimpleJyroModuleHandle("AimAssist", "Sword", true));
        modules.add(new SimpleJyroModuleHandle("TriggerBot", "Sword", false));
        modules.add(new SimpleJyroModuleHandle("Velocity", "Sword", false));
        modules.add(new SimpleJyroModuleHandle("Fullbright", "Render", true));
        modules.add(new SimpleJyroModuleHandle("StorageESP", "Render", false));
        modules.add(new SimpleJyroModuleHandle("TargetHUD", "Render", true));
    }
}
