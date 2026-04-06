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

    public float   scale   = 1.0f;
    public boolean enabled = true;
    public float   xFrac   = 0.01f;
    public float   yFrac   = 0.42f;
    public boolean flicker = true;

    public static PotionHudConfig getInstance() { if (instance == null) load(); return instance; }

    public float   getScale()            { return scale; }
    public void    setScale(float v)     { scale = Math.max(0.5f, Math.min(2.0f, v)); }
    public boolean isEnabled()           { return enabled; }
    public void    setEnabled(boolean v) { enabled = v; }
    public float   getXFrac()            { return xFrac; }
    public void    setXFrac(float v)     { xFrac = Math.max(0f, Math.min(1f, v)); }
    public float   getYFrac()            { return yFrac; }
    public void    setYFrac(float v)     { yFrac = Math.max(0f, Math.min(1f, v)); }
    public boolean isFlicker()           { return flicker; }
    public void    setFlicker(boolean v) { flicker = v; }

    public void resetToDefaults() {
        scale   = 1.0f;
        enabled = true;
        xFrac   = 0.01f;
        yFrac   = 0.42f;
        flicker = true;
    }

    public static void load() {
        File file = PATH.toFile();
        if (file.exists()) {
            try (Reader r = new FileReader(file)) {
                instance = GSON.fromJson(r, PotionHudConfig.class);
            } catch (IOException e) {
                instance = new PotionHudConfig();
            }
        } else {
            instance = new PotionHudConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        File file = PATH.toFile();
        file.getParentFile().mkdirs();
        try (Writer w = new FileWriter(file)) {
            GSON.toJson(instance, w);
        } catch (IOException ignored) {}
    }
}
