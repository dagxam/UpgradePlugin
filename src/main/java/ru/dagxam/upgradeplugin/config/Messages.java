package ru.dagxam.upgradeplugin.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Простая обёртка над messages.yml.
 * Все строки хранятся в одном месте и умеют перезагружаться без рестарта.
 */
public class Messages {

    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        // Кладём messages.yml в папку плагина, если файла ещё нет
        if (!plugin.getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdirs();
        }
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
    }

    public String getRaw(String path, String def) {
        if (config == null) return def;
        return config.getString(path, def);
    }

    public String get(String path, String def) {
        return color(getRaw(path, def));
    }

    public String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
