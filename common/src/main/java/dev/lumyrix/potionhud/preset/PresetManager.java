package dev.lumyrix.potionhud.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class PresetManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class CustomPreset {
        public float   bgTransparency;
        public boolean bgRounded;
        public String  bgColor;
        public float   scale;
        public boolean flicker;
    }

    public static void savePreset(CustomPreset preset, String name, String folderPath) {
        Path dir = (folderPath == null || folderPath.isBlank())
            ? FabricLoader.getInstance().getConfigDir()
            : Path.of(folderPath);
        File file = dir.resolve(name.endsWith(".json") ? name : name + ".json").toFile();
        file.getParentFile().mkdirs();
        try (Writer w = new FileWriter(file)) {
            GSON.toJson(preset, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CustomPreset loadPreset(File file) {
        try (Reader r = new FileReader(file)) {
            return GSON.fromJson(r, CustomPreset.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static CustomPreset snapshot(float bgTransparency, boolean bgRounded,
                                         String bgColor, float scale, boolean flicker) {
        CustomPreset p = new CustomPreset();
        p.bgTransparency = bgTransparency;
        p.bgRounded      = bgRounded;
        p.bgColor        = bgColor;
        p.scale          = scale;
        p.flicker        = flicker;
        return p;
    }
}
