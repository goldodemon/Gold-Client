package dlindustries.vigillant.system.ui.model;

/**
 * Tiny UI-facing module contract. Your existing backend only needs to expose these values;
 * no gameplay/module system code is required inside the renderer.
 */
public interface JyroModuleHandle {
    String name();

    String category();

    boolean enabled();

    void toggle();

    default String description() {
        return "";
    }

    static JyroModuleHandle of(String name, String category, boolean enabled, Runnable toggleAction) {
        return new SimpleJyroModuleHandle(name, category, enabled, toggleAction);
    }
}
