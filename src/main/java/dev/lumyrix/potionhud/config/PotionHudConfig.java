package dev.lumyrix.potionhud.config;

public class PotionHudConfig {
    private static PotionHudConfig INSTANCE = new PotionHudConfig();
    private boolean enabled = true;
    private float scale = 1.0f;
    private float xFrac = 0.02f;
    private float yFrac = 0.02f;
    private float maxHudHeightFrac = 0.5f;
    private boolean flicker = true;
    private HudAnchor anchor = HudAnchor.TOP_LEFT;
    private boolean useAdvancedPosition = false;
    private int offsetX = 10;
    private int offsetY = 10;
    private boolean bgVisible = true;
    private int bgArgb = 0x80000000;
    private boolean bgRounded = true;
    private boolean shadows = true;
    private boolean iconRight = false;
    private boolean previewMode = false;

    public static PotionHudConfig getInstance() { return INSTANCE; }
    public static void save() { }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }
    public float getXFrac() { return xFrac; }
    public void setXFrac(float xFrac) { this.xFrac = xFrac; }
    public float getYFrac() { return yFrac; }
    public void setYFrac(float yFrac) { this.yFrac = yFrac; }
    public float getMaxHudHeightFrac() { return maxHudHeightFrac; }
    public void setMaxHudHeightFrac(float val) { this.maxHudHeightFrac = val; }
    public boolean isFlicker() { return flicker; }
    public void setFlicker(boolean flicker) { this.flicker = flicker; }
    public HudAnchor getAnchor() { return anchor; }
    public boolean isUseAdvancedPosition() { return useAdvancedPosition; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public boolean isBgVisible() { return bgVisible; }
    public int getBgArgb() { return bgArgb; }
    public boolean isBgRounded() { return bgRounded; }
    public boolean isShadows() { return shadows; }
    public boolean isIconRight() { return iconRight; }
    public boolean isPreviewMode() { return previewMode; }
    public void resetToDefaults() {
        this.enabled = true;
        this.scale = 1.0f;
        this.xFrac = 0.02f;
        this.yFrac = 0.02f;
        this.maxHudHeightFrac = 0.5f;
        this.flicker = true;
    }
}
