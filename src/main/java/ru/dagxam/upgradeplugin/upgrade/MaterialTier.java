package ru.dagxam.upgradeplugin.upgrade;

import org.bukkit.Material;

/**
 * Уровень материала для инструментов/оружия/брони.
 * Нужен как стабильный ключ для конфига.
 */
public enum MaterialTier {
    WOODEN,
    STONE,
    COPPER,
    IRON,
    GOLDEN,
    DIAMOND,
    NETHERITE,
    LEATHER,
    CHAINMAIL,
    OTHER;

    public static MaterialTier fromMaterial(Material material) {
        if (material == null) return OTHER;
        String n = material.name();

        if (n.startsWith("WOODEN_")) return WOODEN;
        if (n.startsWith("STONE_")) return STONE;
        if (n.startsWith("COPPER_")) return COPPER;
        if (n.startsWith("IRON_")) return IRON;
        if (n.startsWith("GOLDEN_")) return GOLDEN;
        if (n.startsWith("DIAMOND_")) return DIAMOND;
        if (n.startsWith("NETHERITE_")) return NETHERITE;

        if (n.startsWith("LEATHER_")) return LEATHER;
        if (n.startsWith("CHAINMAIL_")) return CHAINMAIL;

        return OTHER;
    }
}
