package br.com.m4trixdev.data;

import br.com.m4trixdev.Main;
import br.com.m4trixdev.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DataManager {

    private final Main plugin;
    private File file;
    private FileConfiguration data;

    public DataManager(Main plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            try { file.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Nao foi possivel criar data.yml"); }
        }
        reload();
    }

    private void reload() {
        try (InputStreamReader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            data = YamlConfiguration.loadConfiguration(r);
        } catch (IOException e) {
            data = new YamlConfiguration();
        }
    }

    private void save() {
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(data.saveToString());
        } catch (IOException e) {
            plugin.getLogger().severe("Nao foi possivel salvar data.yml");
        }
    }

    public void setSpawn(Location loc) {
        data.set("spawn", LocationUtil.serialize(loc));
        save();
    }

    public Location getSpawn() {
        return LocationUtil.deserialize(data.getString("spawn"));
    }

    public void setSpawnBlockMaterial(Material mat) {
        data.set("spawnblock-material", mat.name());
        save();
    }

    public Material getSpawnBlockMaterial() {
        String raw = data.getString("spawnblock-material");
        if (raw == null || raw.isEmpty()) return null;
        try { return Material.valueOf(raw); }
        catch (IllegalArgumentException e) { return null; }
    }

    public void setAreaPos1(Location loc) {
        data.set("area.pos1", LocationUtil.serialize(loc));
        save();
    }

    public void setAreaPos2(Location loc) {
        data.set("area.pos2", LocationUtil.serialize(loc));
        save();
    }

    public Location getAreaPos1() {
        return LocationUtil.deserialize(data.getString("area.pos1"));
    }

    public Location getAreaPos2() {
        return LocationUtil.deserialize(data.getString("area.pos2"));
    }

    public boolean isAreaConfigured() {
        return getAreaPos1() != null && getAreaPos2() != null;
    }
}
