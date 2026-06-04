package dlindustries.vigillant.system.ui.model;

import java.util.Objects;

/** Mutable demo/adapter implementation used by the standalone jar and tests. */
public final class SimpleJyroModuleHandle implements JyroModuleHandle {
    private final String name;
    private final String category;
    private final Runnable toggleAction;
    private boolean enabled;

    public SimpleJyroModuleHandle(String name, String category, boolean enabled) {
        this(name, category, enabled, null);
    }

    public SimpleJyroModuleHandle(String name, String category, boolean enabled, Runnable toggleAction) {
        this.name = Objects.requireNonNull(name, "name");
        this.category = Objects.requireNonNull(category, "category");
        this.enabled = enabled;
        this.toggleAction = toggleAction;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void toggle() {
        enabled = !enabled;
        if (toggleAction != null) {
            toggleAction.run();
        }
    }
}
