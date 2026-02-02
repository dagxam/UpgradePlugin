package ru.dagxam.upgradeplugin.upgrade;

import org.bukkit.Material;

/**
 * Тип апгрейда (вынесен в enum по запросу).
 * Определяется по типу предмета.
 */
public enum UpgradeType {
    ARMOR,
    TOOL,
    WEAPON,
    NONE;

    public static UpgradeType fromMaterial(Material material) {
        if (material == null) return NONE;
        String n = material.name();

        if (n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS")) {
            return ARMOR;
        }

        // В Minecraft топор — и инструмент, и оружие.
        // Мы считаем его WEAPON, чтобы применять боевые бонусы.
        if (n.endsWith("_SWORD") || n.endsWith("_AXE")) {
            return WEAPON;
        }

        if (n.endsWith("_PICKAXE") || n.endsWith("_SHOVEL") || n.endsWith("_HOE")) {
            return TOOL;
        }

        return NONE;
    }
}
