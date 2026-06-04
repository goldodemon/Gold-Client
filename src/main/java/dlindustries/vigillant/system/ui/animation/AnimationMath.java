package dlindustries.vigillant.system.ui.animation;

/**
 * Small, allocation-free animation helper designed for Minecraft client UI code.
 *
 * <p>Every animation is delta-time based instead of frame-count based, so the HUD and ClickGUI
 * feel the same at 60 FPS and uncapped FPS. The nested {@link AnimatedFloat} class is intentionally
 * tiny and can be embedded in every visual component that needs hover, toggle, fade, scale or slide
 * transitions.</p>
 */
public final class AnimationMath {
    private AnimationMath() {
    }

    public static final float EPSILON = 0.0001F;

    /** Clamps {@code value} into the inclusive {@code [min, max]} range. */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Returns {@code value} clamped to {@code [0, 1]}. */
    public static float saturate(float value) {
        return clamp(value, 0.0F, 1.0F);
    }

    /** Linear interpolation with clamped progress. */
    public static float lerp(float start, float end, float progress) {
        float t = saturate(progress);
        return start + (end - start) * t;
    }

    /** Integer interpolation, useful for alpha values and pixel offsets. */
    public static int lerpInt(int start, int end, float progress) {
        return Math.round(lerp(start, end, progress));
    }

    /** Smoothstep easing. Good default for hover and toggle indicators. */
    public static float easeInOut(float progress) {
        float t = saturate(progress);
        return t * t * (3.0F - 2.0F * t);
    }

    /** Cubic ease-out for snappy slide-in movement that settles softly. */
    public static float easeOutCubic(float progress) {
        float t = 1.0F - saturate(progress);
        return 1.0F - t * t * t;
    }

    /** Cubic ease-in for closing/reverse transitions. */
    public static float easeInCubic(float progress) {
        float t = saturate(progress);
        return t * t * t;
    }

    /**
     * Cubic Bezier easing solved with Newton iterations and a binary-search fallback.
     * Control points should be in CSS-style normalized coordinates.
     */
    public static float cubicBezier(float progress, float x1, float y1, float x2, float y2) {
        float x = saturate(progress);
        float t = x;

        for (int i = 0; i < 6; i++) {
            float currentX = sampleCubic(t, x1, x2) - x;
            float derivative = sampleCubicDerivative(t, x1, x2);
            if (Math.abs(currentX) < EPSILON || Math.abs(derivative) < EPSILON) {
                break;
            }
            t = saturate(t - currentX / derivative);
        }

        float lower = 0.0F;
        float upper = 1.0F;
        for (int i = 0; i < 8 && Math.abs(sampleCubic(t, x1, x2) - x) > EPSILON; i++) {
            if (sampleCubic(t, x1, x2) < x) {
                lower = t;
            } else {
                upper = t;
            }
            t = (lower + upper) * 0.5F;
        }

        return sampleCubic(t, y1, y2);
    }

    /** Sleek UI default easing: quick, restrained, and not bouncy. */
    public static float jyroEase(float progress) {
        return cubicBezier(progress, 0.22F, 1.0F, 0.36F, 1.0F);
    }

    /** Returns an ARGB color using a monochrome RGB value and clamped alpha percentage. */
    public static int alpha(int rgb, float opacity) {
        int a = lerpInt(0, 255, saturate(opacity));
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    /** Returns an ARGB color with an integer alpha channel. */
    public static int argb(int alpha, int rgb) {
        return ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }

    /** Monotonic seconds for frame delta calculations. */
    public static double nowSeconds() {
        return System.nanoTime() / 1_000_000_000.0D;
    }

    /** Computes a safe delta value in seconds, clamped to avoid huge jumps after tabbing back in. */
    public static float deltaSeconds(double[] previousTime) {
        double now = nowSeconds();
        if (previousTime[0] <= 0.0D) {
            previousTime[0] = now;
            return 0.0F;
        }
        float delta = (float) (now - previousTime[0]);
        previousTime[0] = now;
        return clamp(delta, 0.0F, 0.05F);
    }

    private static float sampleCubic(float t, float c1, float c2) {
        float inv = 1.0F - t;
        return 3.0F * inv * inv * t * c1 + 3.0F * inv * t * t * c2 + t * t * t;
    }

    private static float sampleCubicDerivative(float t, float c1, float c2) {
        float inv = 1.0F - t;
        return 3.0F * inv * inv * c1 + 6.0F * inv * t * (c2 - c1) + 3.0F * t * t * (1.0F - c2);
    }

    /**
     * Reusable animated float. Speed is expressed as "units per second" toward the target.
     */
    public static final class AnimatedFloat {
        private float value;
        private float target;
        private float speed;

        public AnimatedFloat(float initialValue, float speed) {
            this.value = initialValue;
            this.target = initialValue;
            this.speed = Math.max(EPSILON, speed);
        }

        public float update(float deltaSeconds) {
            float distance = target - value;
            if (Math.abs(distance) <= EPSILON) {
                value = target;
                return value;
            }

            float step = saturate(deltaSeconds * speed);
            value = lerp(value, target, jyroEase(step));
            if (Math.abs(target - value) <= EPSILON) {
                value = target;
            }
            return value;
        }

        public AnimatedFloat target(float target) {
            this.target = target;
            return this;
        }

        public AnimatedFloat speed(float speed) {
            this.speed = Math.max(EPSILON, speed);
            return this;
        }

        public AnimatedFloat snap(float value) {
            this.value = value;
            this.target = value;
            return this;
        }

        public float value() {
            return value;
        }

        public float target() {
            return target;
        }

        public boolean isAtTarget() {
            return Math.abs(value - target) <= EPSILON;
        }
    }
}
