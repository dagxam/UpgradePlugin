package ru.dagxam.upgradeplugin.upgrade;

import org.bukkit.Material;

/**
 * Табличные базовые значения ванильных инструментов/оружия.
 * Храним отдельно, чтобы UpgradeManager был компактнее.
 */
public final class VanillaStats {

    private VanillaStats() {}

    public static double baseDamage(Material type) {
        String n = type.name();

        // МЕЧИ
        if (n.endsWith("_SWORD")) {
            if (n.startsWith("WOODEN_") || n.startsWith("GOLDEN_")) return 4.0;
            if (n.startsWith("STONE_")) return 5.0;
            if (n.startsWith("COPPER_")) return 5.0;
            if (n.startsWith("IRON_")) return 6.0;
            if (n.startsWith("DIAMOND_")) return 7.0;
            if (n.startsWith("NETHERITE_")) return 8.0;
        }

        // ТОПОРЫ
        if (n.endsWith("_AXE")) {
            if (n.startsWith("WOODEN_")) return 7.0;
            if (n.startsWith("STONE_")) return 9.0;
            if (n.startsWith("COPPER_")) return 8.0;
            if (n.startsWith("IRON_")) return 9.0;
            if (n.startsWith("GOLDEN_")) return 7.0;
            if (n.startsWith("DIAMOND_")) return 9.0;
            if (n.startsWith("NETHERITE_")) return 10.0;
        }

        // КИРКИ
        if (n.endsWith("_PICKAXE")) {
            if (n.startsWith("WOODEN_")) return 2.0;
            if (n.startsWith("STONE_")) return 3.0;
            if (n.startsWith("COPPER_")) return 3.5;
            if (n.startsWith("IRON_")) return 4.0;
            if (n.startsWith("GOLDEN_")) return 2.0;
            if (n.startsWith("DIAMOND_")) return 5.0;
            if (n.startsWith("NETHERITE_")) return 6.0;
        }

        // ЛОПАТЫ
        if (n.endsWith("_SHOVEL")) {
            if (n.startsWith("WOODEN_")) return 2.5;
            if (n.startsWith("STONE_")) return 3.5;
            if (n.startsWith("COPPER_")) return 4.0;
            if (n.startsWith("IRON_")) return 4.5;
            if (n.startsWith("GOLDEN_")) return 2.5;
            if (n.startsWith("DIAMOND_")) return 5.5;
            if (n.startsWith("NETHERITE_")) return 6.5;
        }

        // МОТЫГИ
        if (n.endsWith("_HOE")) {
            return 1.0;
        }

        return 1.0;
    }

    public static double baseSpeed(Material type) {
        String n = type.name();

        if (n.endsWith("_SWORD")) return 1.6;

        if (n.endsWith("_AXE")) {
            if (n.startsWith("WOODEN_") || n.startsWith("STONE_")) return 0.8;
            if (n.startsWith("COPPER_") || n.startsWith("IRON_")) return 0.9;
            if (n.startsWith("GOLDEN_") || n.startsWith("DIAMOND_") || n.startsWith("NETHERITE_")) return 1.0;
        }

        if (n.endsWith("_PICKAXE")) return 1.2;
        if (n.endsWith("_SHOVEL")) return 1.0;

        if (n.endsWith("_HOE")) {
            if (n.startsWith("WOODEN_")) return 1.0;
            if (n.startsWith("STONE_")) return 2.0;
            if (n.startsWith("COPPER_")) return 2.5;
            if (n.startsWith("IRON_")) return 3.0;
            if (n.startsWith("GOLDEN_")) return 1.0;
            if (n.startsWith("DIAMOND_")) return 4.0;
            if (n.startsWith("NETHERITE_")) return 4.0;
        }

        return 4.0;
    }
}
