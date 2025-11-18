package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.dagxam.upgradeplugin.UpgradePlugin;
import ru.dagxam.upgradeplugin.items.ItemManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnvilListener implements Listener {

    private final UpgradePlugin plugin;

    // маркер улучшения, которым пользуется BlockBreakListener
    private static final String UPGRADED_LORE_STRING = "§b[Улучшено]";

    public AnvilListener(UpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);   // предмет
        ItemStack secondItem = inventory.getItem(1);  // книга

        if (firstItem == null || secondItem == null || !ItemManager.isUpgradeBook(secondItem)) {
            return;
        }

        ItemStack result = firstItem.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        if (lore.contains(UPGRADED_LORE_STRING)) {
            event.setResult(null);
            return;
        }

        Material type = result.getType();
        boolean upgraded = false;

        if (isArmor(type)) upgraded = upgradeArmor(meta, type);
        else if (isToolOrWeapon(type)) upgraded = upgradeToolOrWeapon(meta, type);

        if (!upgraded) return;

        lore.add(UPGRADED_LORE_STRING);
        meta.setLore(lore);
        result.setItemMeta(meta);

        event.setResult(result);
        inventory.setRepairCost(20);
    }

    private boolean isArmor(Material type) {
        String n = type.name();
        return n.endsWith("_HELMET")
                || n.endsWith("_CHESTPLATE")
                || n.endsWith("_LEGGINGS")
                || n.endsWith("_BOOTS");
    }

    private boolean isToolOrWeapon(Material type) {
        String n = type.name();
        return n.endsWith("_SWORD")
                || n.endsWith("_AXE")
                || n.endsWith("_PICKAXE")
                || n.endsWith("_SHOVEL")
                || n.endsWith("_HOE");
    }


    // ======================
    // УЛУЧШЕНИЕ БРОНИ
    // ======================
    private boolean upgradeArmor(ItemMeta meta, Material type) {
        String name = type.name();

        int prot = 4;
        int unbreak = 3;
        int thorns = 0;

        if (name.startsWith("LEATHER_")) {
            prot = 3; unbreak = 2;
        } else if (name.startsWith("CHAINMAIL_")) {
            prot = 3;
        } else if (name.startsWith("IRON_") || name.startsWith("COPPER_")) {
            prot = 4;
        } else if (name.startsWith("GOLDEN_")) {
            prot = 4; unbreak = 4;
        } else if (name.startsWith("DIAMOND_")) {
            prot = 5; unbreak = 4;
        } else if (name.startsWith("NETHERITE_")) {
            prot = 6; unbreak = 5; thorns = 3;
        }

        meta.addEnchant(Enchantment.PROTECTION, prot, true);
        meta.addEnchant(Enchantment.UNBREAKING, unbreak, true);
        if (thorns > 0) meta.addEnchant(Enchantment.THORNS, thorns, true);

        return true;
    }


    // ======================
    // УЛУЧШЕНИЕ ОРУЖИЯ / ИНСТРУМЕНТОВ
    // ======================
    private boolean upgradeToolOrWeapon(ItemMeta meta, Material type) {
        String name = type.name();

        // Все виды получают Efficiency / Unbreaking
        if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        }

        // Мечи и топоры — Sharpness
        if (name.endsWith("_SWORD") || name.endsWith("_AXE")) {
            meta.addEnchant(Enchantment.SHARPNESS, 6, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        }

        applyMaterialCombatBonus(meta, type);

        return true;
    }


    // ======================
    // ГЛАВНАЯ ЧАСТЬ:
    // Урон и скорость по твоим числам
    // ======================
    private void applyMaterialCombatBonus(ItemMeta meta, Material type) {

        double bonus = getBonusForMaterial(type);

        // убираем старые атрибуты, если были
        meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.ATTACK_SPEED);

        // добавляем новые
        AttributeModifier dmg = new AttributeModifier(
                UUID.randomUUID(),
                "upgrade_dmg",
                bonus,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
        );

        AttributeModifier spd = new AttributeModifier(
                UUID.randomUUID(),
                "upgrade_speed",
                bonus,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
        );

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, dmg);
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, spd);
    }


    // ======================
    // Система бонусов по материалам
    // ======================
    private double getBonusForMaterial(Material type) {
        String n = type.name();

        if (n.startsWith("WOODEN_")) return 2;
        if (n.startsWith("STONE_")) return 4;
        if (n.startsWith("COPPER_")) return 5;
        if (n.startsWith("IRON_")) return 8;
        if (n.startsWith("GOLDEN_")) return 9;
        if (n.startsWith("DIAMOND_")) return 12;
        if (n.startsWith("NETHERITE_")) return 15;

        return 0;
    }
}
