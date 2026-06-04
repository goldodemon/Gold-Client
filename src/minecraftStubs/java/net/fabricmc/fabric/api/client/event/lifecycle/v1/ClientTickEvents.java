package net.fabricmc.fabric.api.client.event.lifecycle.v1;
import net.minecraft.client.MinecraftClient;
public final class ClientTickEvents {
    public static final EndTick END_CLIENT_TICK = new EndTick();
    public interface EndTickCallback { void onEndTick(MinecraftClient client); }
    public static final class EndTick { public void register(EndTickCallback callback) {} }
}
