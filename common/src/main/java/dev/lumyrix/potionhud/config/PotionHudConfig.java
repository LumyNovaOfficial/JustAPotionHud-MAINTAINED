package dev.lumyrix.potionhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class PotionHudConfig {

    private static PotionHudConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("justapotionhud.json");

    public float   scale               = 1.0f;
    public boolean enabled             = true;
    public boolean flicker             = true;
    public boolean iconRight           = false;
    public boolean previewMode         = false;
    public boolean hideVanillaHud      = false;

    public String  anchor              = "CENTER_LEFT";
    public int     offsetX             = 5;
    public int     offsetY             = 5;

    public float   posXFrac            = 0.02f;
    public float   posYFrac            = 0.50f;
    public boolean useAltPos           = false;

    public String  preset              = "feather";
    public float   bgTransparency      = 0.45f;
    public boolean bgRounded           = true;
    public String  bgColor             = "#000000";
    public boolean shadows             = false;

    public float   maxHudHeightFrac    = 0.43f;
    public int     maxEffectsOverride  = -1;

    public static PotionHudConfig getInstance() { if (instance == null) load(); return instance; }

    public float      getScale()                       { return scale; }
    public void       setScale(float v)                { scale = Math.max(0.5f, Math.min(3.0f, v)); }
    public boolean    isEnabled()                      { return enabled; }
    public void       setEnabled(boolean v)            { enabled = v; }
    public boolean    isFlicker()                      { return flicker; }
    public void       setFlicker(boolean v)            { flicker = v; }
    public boolean    isIconRight()                    { return iconRight; }
    public void       setIconRight(boolean v)          { iconRight = v; }
    public boolean    isPreviewMode()                  { return previewMode; }
    public void       setPreviewMode(boolean v)        { previewMode = v; }
    public boolean    isHideVanillaHud()               { return hideVanillaHud; }
    public void       setHideVanillaHud(boolean v)     { hideVanillaHud = v; }

    public HudAnchor  getAnchor()                      { try { return HudAnchor.valueOf(anchor); } catch (Exception e) { return HudAnchor.CENTER_LEFT; } }
    public void       setAnchor(HudAnchor v)           { anchor = v.name(); }
    public int        getOffsetX()                     { return offsetX; }
    public void       setOffsetX(int v)                { offsetX = v; }
    public int        getOffsetY()                     { return offsetY; }
    public void       setOffsetY(int v)                { offsetY = v; }

    public float      getPosXFrac()                    { return posXFrac; }
    public void       setPosXFrac(float v)             { posXFrac = Math.max(0f, Math.min(1f, v)); }
    public float      getPosYFrac()                    { return posYFrac; }
    public void       setPosYFrac(float v)             { posYFrac = Math.max(0f, Math.min(1f, v)); }
    public boolean    isUseAltPos()                    { return useAltPos; }
    public void       setUseAltPos(boolean v)          { useAltPos = v; }

    public String     getPreset()                      { return preset; }
    public void       setPreset(String v)              { preset = v; }
    public float      getBgTransparency()              { return bgTransparency; }
    public void       setBgTransparency(float v)       { bgTransparency = Math.max(0f, Math.min(1f, v)); }
    public boolean    isBgRounded()                    { return bgRounded; }
    public void       setBgRounded(boolean v)          { bgRounded = v; }
    public String     getBgColor()                     { return bgColor; }
    public void       setBgColor(String v)             { bgColor = v; }
    public boolean    isShadows()                      { return false; } // removed
    public void       setShadows(boolean v)            { /* no-op */ }
    public float      getMaxHudHeightFrac()            { return maxHudHeightFrac; }
    public void       setMaxHudHeightFrac(float v)     { maxHudHeightFrac = Math.max(0.1f, Math.min(1.0f, v)); }
    public int        getMaxEffectsOverride()          { return maxEffectsOverride; }
    public void       setMaxEffectsOverride(int v)     { maxEffectsOverride = v; }

    public int getBgArgb() {
        if (bgTransparency >= 1.0f) return 0;
        int alpha = (int)((1.0f - bgTransparency) * 255);
        try {
            String hex = bgColor.startsWith("#") ? bgColor.substring(1) : bgColor;
            if (hex.length() == 6) return (alpha << 24) | (Integer.parseInt(hex, 16) & 0x00FFFFFF);
        } catch (NumberFormatException ignored) {}
        return (alpha << 24);
    }

    public boolean isBgVisible() { return bgTransparency < 1.0f; }

    public int[] resolvePosition(int screenW, int screenH, int hudW, int hudH) {
        HudAnchor anc = getAnchor();
        int x, y;
        switch (anc) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT    -> x = offsetX;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> x = screenW - hudW - offsetX;
            default                                    -> x = screenW / 2 - hudW / 2 + offsetX;
        }
        switch (anc) {
            case TOP_LEFT, TOP_RIGHT                   -> y = offsetY;
            case BOTTOM_LEFT, BOTTOM_RIGHT             -> y = screenH - hudH - offsetY;
            case CENTER_LEFT, CENTER_RIGHT             -> y = screenH / 2 - hudH / 2 + offsetY;
            default                                    -> y = screenH / 2 - hudH / 2 + offsetY;
        }
        return new int[]{ x, y };
    }

    public void applyPreset(String name) {
        switch (name) {
            case "og"      -> { bgTransparency = 1.0f;  bgRounded = false; bgColor = "#000000"; shadows = false; }
            case "feather" -> { bgTransparency = 0.45f; bgRounded = true;  bgColor = "#000000"; shadows = false; }
            case "lunar"   -> { bgTransparency = 0.50f; bgRounded = false; bgColor = "#000000"; shadows = false; }
        }
        preset = name;
    }

    public void resetToDefaults() {
        scale = 1.0f; enabled = true; flicker = true; iconRight = false; previewMode = false;
        hideVanillaHud = false;
        anchor = "CENTER_LEFT"; offsetX = 5; offsetY = 5;
        posXFrac = 0.02f; posYFrac = 0.50f; useAltPos = false;
        maxHudHeightFrac = 0.43f; maxEffectsOverride = -1;
        applyPreset("feather");
    }

    public static void load() {
        File file = PATH.toFile();
        if (file.exists()) {
            try (Reader r = new FileReader(file)) {
                instance = GSON.fromJson(r, PotionHudConfig.class);
                if (instance == null) instance = new PotionHudConfig();
            } catch (IOException e) { instance = new PotionHudConfig(); }
        } else {
            instance = new PotionHudConfig();
            instance.applyPreset("feather");
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        File file = PATH.toFile();
        file.getParentFile().mkdirs();
        try (Writer w = new FileWriter(file)) { GSON.toJson(instance, w); }
        catch (IOException ignored) {}
    }
}
