package ru.dagxam.upgradeplugin.listeners;

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

    public AnvilListener(UpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        // Мы снова будем использовать 'inventory' для setRepairCost
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0); 
        ItemStack secondItem = inventory.getItem(1); 

        if (firstItem == null || secondItem == null || !ItemManager.isUpgradeBook(secondItem)) {
            return;
        }

        ItemStack resultItem = firstItem.clone();
        ItemMeta meta = resultItem.getItemMeta();
        if (meta == null) {
            return;
        }
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        if (lore.contains("§b[Улучшено]")) {
             event.setResult(null);
             return;
        }

        Material type = resultItem.getType();
        boolean success = false;

        // --- ЛОГИКА УЛУЧШЕНИЯ ---
        
        // Кожа
        if (type.name().startsWith("LEATHER_")) {
            applyAttribute(meta, Attribute.GENERIC_ARMOR, 2.0, "UpgradeArmor", getSlot(type));
            applyAttribute(meta, Attribute.GENERIC_MAX_HEALTH, 4.0, "UpgradeHealth", getSlot(type)); // 2 сердца
            applyDurability(meta, 2);
            success = true;
        }
        // Кольчуга
        else if (type.name().startsWith("CHAINMAIL_")) {
            applyAttribute(meta, Attribute.GENERIC_ARMOR, 4.0, "UpgradeArmor", getSlot(type));
            applyAttribute(meta, Attribute.GENERIC_MAX_HEALTH, 8.0, "UpgradeHealth", getSlot(type)); // 4 сердца
            applyDurability(meta, 4);
            success = true;
        }
        // Железо
        else if (type.name().startsWith("IRON_")) {
            if (getSlot(type) != null) { // Броня
                applyAttribute(meta, Attribute.GENERIC_ARMOR, 6.0, "UpgradeArmor", getSlot(type));
                applyAttribute(meta, Attribute.GENERIC_MAX_HEALTH, 12.0, "UpgradeHealth", getSlot(type)); // 6 сердец
                applyDurability(meta, 6);
                success = true;
            } else { // Инструменты/Оружие
                applyAttribute(meta, Attribute.GENERIC_ATTACK_DAMAGE, 8.0, "UpgradeDamage", EquipmentSlot.HAND);
                applyAttribute(meta, Attribute.GENERIC_ATTACK_SPEED, 8.0, "UpgradeAtkSpeed", EquipmentSlot.HAND);
                success = true;
            }
        }
        // Золото
        else if (type.name().startsWith("GOLDEN_")) {
             if (getSlot(type) != null) { // Броня
                applyAttribute(meta, Attribute.GENERIC_ARMOR, 7.0, "UpgradeArmor", getSlot(type));
                applyAttribute(meta, Attribute.GENERIC_MAX_HEALTH, 14.0, "UpgradeHealth", getSlot(type)); // 7 сердец
                applyDurability(meta, 7);
                success = true;
            } else { // Инструменты/Оружие
                applyAttribute(meta, Attribute.GENERIC_ATTACK_DAMAGE, 9.0, "UpgradeDamage", EquipmentSlot.HAND);
                applyAttribute(meta, Attribute.GENERIC_ATTACK_SPEED, 9.0, "UpgradeAtkSpeed", EquipmentSlot.HAND);
                success = true;
            }
        }
        // Алмазы
        else if (type.name().startsWith("DIAMOND_")) {
             if (getSlot(type) != null) { // Броня
                applyAttribute(meta, Attribute.GENERIC_ARMOR, 10.0, "UpgradeArmor", getSlot(type));
                applyAttribute(meta, Attribute.GENERIC_ARMOR_TOUGHNESS, 10.0, "UpgradeToughness", getSlot(type));
                applyAttribute(meta, Attribute.GENERIC_MAX_HEALTH, 20.0, "UpgradeHealth", getSlot(type)); // 10 сердец
                applyDurability(meta, 10);
                success = true;
            } else { // Инструменты/Оружие
                applyAttribute(meta, Attribute.GENERIC_ATTACK_DAMAGE, 12.0, "UpgradeDamage", EquipmentSlot.HAND);
                applyAttribute(meta, Attribute.GENERIC_ATTACK_SPEED, 12.0, "UpgradeAtkSpeed", EquipmentSlot.HAND);
                success = true;
            }
        }
        // Незерит
        else if (type.name().startsWith("NETHERITE_")) {
             if (getSlot(type) != null) { // Броня
                applyAttribute(meta, Attribute.GENERIC_ARMOR, 15.0, "UpgradeArmor", getSlot(type));
                applyAttribute(meta, Attribute.GENERIC_ARMOR_TOUGHNESS, 15.0, "UpgradeToughness", getSlot(type));
                applyAttribute(meta, Attribute.GENERIC_KNOCKBACK_RESISTANCE, 1.5, "UpgradeKnockback", getSlot(type)); // 1.5 = 150%
                applyAttribute(meta, Attribute.GENERIC_MAX_HEALTH, 30.0, "UpgradeHealth", getSlot(type)); // 15 сердец
                applyDurability(meta, 15);
                success = true;
            } else { // Инструменты/Оружие
                applyAttribute(meta, Attribute.GENERIC_ATTACK_DAMAGE, 15.0, "UpgradeDamage", EquipmentSlot.HAND);
                applyAttribute(meta, Attribute.GENERIC_ATTACK_SPEED, 15.0, "UpgradeAtkSpeed", EquipmentSlot.HAND);
                success = true;
            }
        }
        // Дерево
        else if (type.name().startsWith("WOODEN_")) {
             applyAttribute(meta, Attribute.GENERIC_ATTACK_DAMAGE, 2.0, "UpgradeDamage", EquipmentSlot.HAND);
             applyAttribute(meta, Attribute.GENERIC_ATTACK_SPEED, 2.0, "UpgradeAtkSpeed", EquipmentSlot.HAND);
             success = true;
        }
        // Камень
        else if (type.name().startsWith("STONE_")) {
             applyAttribute(meta, Attribute.GENERIC_ATTACK_DAMAGE, 4.0, "UpgradeDamage", EquipmentSlot.HAND);
             applyAttribute(meta, Attribute.GENERIC_ATTACK_SPEED, 4.0, "UpgradeAtkSpeed", EquipmentSlot.HAND);
             success = true;
        }

        // --- КОНЕЦ ЛОГИКИ ---

        if (success) {
            lore.add("§b[Улучшено]");
            meta.setLore(lore);
            resultItem.setItemMeta(meta);

            event.setResult(resultItem);
            
            // ИСПРАВЛЕНО: Мы ВОЗВРАЩАЕМ этот код. 
            // Это правильно, хоть и "устарело".
            inventory.setRepairCost(20);
        }
    }

    private void applyAttribute(ItemMeta meta, Attribute attribute, double amount, String name, EquipmentSlot slot) {
        if (slot == null) slot = EquipmentSlot.HAND; 
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), name, amount, AttributeModifier.Operation.ADD_NUMBER, slot);
        meta.addAttributeModifier(attribute, modifier);
    }

    private void applyDurability(ItemMeta meta, int level) {
        int currentLevel = meta.getEnchantLevel(Enchantment.UNBREAKING);
        meta.addEnchant(Enchantment.UNBREAKING, currentLevel + level, true);
    }

    private EquipmentSlot getSlot(Material type) {
        String name = type.name();
        if (name.endsWith("_HELMET")) return EquipmentSlot.HEAD;
        if (name.endsWith("_CHESTPLATE")) return EquipmentSlot.CHEST;
        if (name.endsWith("_LEGGINGS")) return EquipmentSlot.LEGS;
        if (name.endsWith("_BOOTS")) return EquipmentSlot.FEET;
        return null;
    }
}
