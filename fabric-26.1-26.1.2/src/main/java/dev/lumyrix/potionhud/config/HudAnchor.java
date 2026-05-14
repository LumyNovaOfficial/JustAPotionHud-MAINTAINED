package dev.lumyrix.potionhud.config;

public enum HudAnchor {
    TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT,
    TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT,
    CENTER;

    public String label() {
        return switch (this) {
            case TOP_LEFT     -> "Top Left";
            case CENTER_LEFT  -> "Center Left";
            case BOTTOM_LEFT  -> "Bottom Left";
            case TOP_RIGHT    -> "Top Right";
            case CENTER_RIGHT -> "Center Right";
            case BOTTOM_RIGHT -> "Bottom Right";
            case CENTER       -> "Center";
        };
    }

    public HudAnchor next() {
        HudAnchor[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    public HudAnchor prev() {
        HudAnchor[] v = values();
        return v[(ordinal() + v.length - 1) % v.length];
    }
}
