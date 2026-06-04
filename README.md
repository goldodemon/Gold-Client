# Jyro-Client

Jyro-Client is a sleek monochrome Minecraft Fabric client UI shell targeting Minecraft 1.21.11.

## Build

```bash
gradle build
```

The jar is produced under `build/libs/`.

This repository includes a tiny `src/minecraftStubs/java` compile-only source set so the UI shell can be built in restricted environments that cannot download Fabric/Minecraft artifacts. Those stubs are **not** packaged into the jar; they only provide compile-time symbols for the UI classes. If you already have a normal Fabric Loom workspace, you can drop `src/main/java/dlindustries/vigillant/system/ui` and `src/client/java/dlindustries/vigillant/system/client` into it and wire `JyroModuleHandle` to your real module manager.

## Runtime controls

- Right Shift opens the Jyro ClickGUI demo shell.
- Click a module slot to toggle its animated indicator bar.
- Right-click a category header to collapse/expand it.
- Drag category headers to reposition frames.
- Click the search box or press Ctrl+F to filter modules.
- Escape clears search first, then smoothly closes the GUI.

## Backend integration

The UI uses the tiny `JyroModuleHandle` interface so your existing module system can adapt modules without moving gameplay logic into the renderer.
