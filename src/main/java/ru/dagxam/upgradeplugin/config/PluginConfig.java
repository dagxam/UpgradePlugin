package ru.dagxam.upgradeplugin.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import ru.dagxam.upgradeplugin.upgrade.MaterialTier;
import ru.dagxam.upgradeplugin.upgrade.UpgradeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Единая точка доступа к config.yml.
 * Вынесено, чтобы увести хардкод из листенеров.
 */
public class PluginConfig {

    private final JavaPlugin plugin;

    // Кэш для часто дергаемых чисел (удары добычи)
    private final Map<String, Integer> intCache = new ConcurrentHashMap<>();

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.reloadConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        intCache.clear();
    }

    public String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public List<String> colorList(List<String> list) {
        if (list == null) return Collections.emptyList();
        List<String> out = new ArrayList<>(list.size());
        for (String s : list) out.add(color(s));
        return out;
    }

    // ===== Book =====

    public String getBookName() {
        return color(plugin.getConfig().getString("book.name", "&bКнига Улучшения"));
    }

    public List<String> getBookLore() {
        return colorList(plugin.getConfig().getStringList("book.lore"));
    }

    public int getAnvilRepairCost() {
        return plugin.getConfig().getInt("anvil.repair_cost", 20);
    }

    public String getUpgradedLoreMarker() {
        return color(plugin.getConfig().getString("markers.lore_upgraded", "&b[Улучшено]"));
    }

    // ===== Villager =====

    public int getVillagerChancePercent() {
        return plugin.getConfig().getInt("villager.trade_chance_percent", 50);
    }

    public int getVillagerEmeraldCost() {
        return plugin.getConfig().getInt("villager.emerald_cost", 20);
    }

    public int getVillagerMaxUses() {
        return plugin.getConfig().getInt("villager.max_uses", 3);
    }

    public int getVillagerXp() {
        return plugin.getConfig().getInt("villager.xp", 15);
    }

    public float getVillagerPriceMultiplier() {
        return (float) plugin.getConfig().getDouble("villager.price_multiplier", 0.05);
    }

    // ===== Upgrades =====

    public int getArmorEnchant(MaterialTier tier, String enchantKey, int def) {
        String path = "upgrades.armor." + tier.name() + "." + enchantKey;
        return plugin.getConfig().getInt(path, def);
    }

    public int getToolEnchant(String enchantKey, int def) {
        return plugin.getConfig().getInt("upgrades.tool." + enchantKey, def);
    }

    public int getWeaponEnchant(String enchantKey, int def) {
        return plugin.getConfig().getInt("upgrades.weapon." + enchantKey, def);
    }

    public double getCombatBonus(MaterialTier tier) {
        return plugin.getConfig().getDouble("upgrades.combat_bonus." + tier.name(), 0.0);
    }

    // ===== Mining hits =====

    public boolean isMiningEnabled() {
        return plugin.getConfig().getBoolean("mining.enabled", true);
    }

    public int getMiningHits(Material blockType, MaterialTier pickTier, int def) {
        String blockKey = blockType.name();
        String cacheKey = "mining.hits." + blockKey + "." + pickTier.name();
        return intCache.computeIfAbsent(cacheKey, k -> {
            int v = plugin.getConfig().getInt(k, Integer.MIN_VALUE);
            if (v != Integer.MIN_VALUE) return v;
            // fallback на секцию "OTHER" для всех блоков
            String fallback = "mining.hits.OTHER." + pickTier.name();
            return plugin.getConfig().getInt(fallback, def);
        });
    }

    // ===== Validation (легкая) =====

    public void warnIfMisconfigured() {
        ConfigurationSection armor = plugin.getConfig().getConfigurationSection("upgrades.armor");
        if (armor == null) {
            plugin.getLogger().warning("В config.yml нет секции upgrades.armor — будут использованы дефолты.");
        }
        if (plugin.getConfig().getConfigurationSection("mining.hits") == null) {
            plugin.getLogger().warning("В config.yml нет секции mining.hits — добыча будет по дефолтам.");
        }
    }
}
