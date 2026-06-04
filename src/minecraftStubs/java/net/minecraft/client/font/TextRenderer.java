package net.minecraft.client.font;
public class TextRenderer { public int getWidth(String text) { return text == null ? 0 : text.length() * 6; } }
