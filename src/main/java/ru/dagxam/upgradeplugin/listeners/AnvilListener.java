package ru.dagxam.upgradeplugin.listeners;

import com.google.common.collect.Multimap;
import org.bukkit.ChatColor; // <-- НУЖЕН ЭТОТ ИМПОРТ
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
        
        // ИСПРАВЛЕНО: Получаем "чистое" имя предмета для проверки медной кирки
        String displayName = meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : "";

        // --- ЛОГИКА УЛУЧШЕНИЯ ---

        // ИСПРАВЛЕНО: Проверка медной кирки по имени
        if (displayName.equalsIgnoreCase("Медная кирка") || displayName.equalsIgnoreCase("Copper Pickaxe")) {
            applyWeaponBonus(meta, type, 6.0, 6.0); // +6 урон, +6 скорость
            // ИСПРАВЛЕНО: DIG_SPEED -> EFFICIENCY
            meta.addEnchant(Enchantment.EFFICIENCY, 12, true); // "8 ударов"
            success = true;
        }
        // Кожа
        else if (type.name().startsWith("LEATHER_")) {
            applyArmorBonus(meta, type, getSlot(type), 2.0, 4.0, 0, 0); 
            applyDurability(meta, 2);
            success = true;
        }
        // Кольчуга
        else if (type.name().startsWith("CHAINMAIL_")) {
            applyArmorBonus(meta, type, getSlot(type), 4.0, 8.0, 0, 0); 
            applyDurability(meta, 4);
            success = true;
        }
        // Железо
        else if (type.name().startsWith("IRON_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 6.0, 12.0, 0, 0); 
                applyDurability(meta, 6);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 8.0, 8.0);
                if (type == Material.IRON_PICKAXE) {
                    // ИСПРАВЛЕНО: DIG_SPEED -> EFFICIENCY
                    meta.addEnchant(Enchantment.EFFICIENCY, 15, true); 
                }
                success = true;
            }
        }
        // Золото
        else if (type.name().startsWith("GOLDEN_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 7.0, 14.0, 0, 0);
                applyDurability(meta, 7);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 9.0, 9.0);
                if (type == Material.GOLDEN_PICKAXE) {
                    // ИСПРАВЛЕНО: DIG_SPEED -> EFFICIENCY
                    meta.addEnchant(Enchantment.EFFICIENCY, 12, true);
                }
                success = true;
            }
        }
        // Алмазы
        else if (type.name().startsWith("DIAMOND_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 10.0, 20.0, 10.0, 0); 
                applyDurability(meta, 10);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 12.0, 12.0);
                if (type == Material.DIAMOND_PICKAXE) {
                    // ИСПРАВЛЕНО: DIG_SPEED -> EFFICIENCY
                    meta.addEnchant(Enchantment.EFFICIENCY, 20, true);
                }
                success = true;
            }
        }
        // Незерит
        else if (type.name().startsWith("NETHERITE_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 15.0, 30.0, 15.0, 1.5); 
                applyDurability(meta, 15);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 15.0, 15.0);
                if (type == Material.NETHERITE_PICKAXE) {
                    // ИСПРАВЛЕНО: DIG_SPEED -> EFFICIENCY
                    meta.addEnchant(Enchantment.EFFICIENCY, 25, true);
                }
                success = true;
            }
        }
        // Дерево
        else if (type.name().startsWith("WOODEN_")) {
            applyWeaponBonus(meta, type, 2.0, 2.0);
            if (type == Material.WOODEN_PICKAXE) {
                // ИСПРАВЛЕНО: DIG_SPEED -> EFFICIENCY
                meta.addEnchant(Enchantment.EFFICIENCY, 8, true);
            }
            success = true;
        }
        // Камень
        else if (type.name().startsWith("STONE_")) {
            applyWeaponBonus(meta, type, 4.0, 4.0);
            if (type == Material.STONE_PICKAXE) {
                // ИСПРАВЛЕНО: DIG_SPEED -> EFFICIENCY
                meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
            }
            success = true;
        }

        // --- КОНЕЦ ЛОГИКИ ---

        if (success) {
            lore.add("§b[Улучшено]");
            meta.setLore(lore);
            resultItem.setItemMeta(meta);
            event.setResult(resultItem);
            inventory.setRepairCost(20);
        }
    }

    /**
     * Применяет бонусы к ОРУЖИЮ/ИНСТРУМЕНТАМ, суммируя их с ванильными.
     */
    private void applyWeaponBonus(ItemMeta meta, Material type, double damageBonus, double speedBonus) {
        // ИСПРАВЛЕНО: Используем ванильные статы для "Медной кирки" (из вашего скриншота)
        String displayName = meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : "";
        boolean isCopperPick = displayName.equalsIgnoreCase("Медная кирка") || displayName.equalsIgnoreCase("Copper Pickaxe");

        double baseDamage = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_DAMAGE, isCopperPick);
        double newDamage = baseDamage + damageBonus;
        
        double baseSpeed = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_SPEED, isCopperPick);
        double newSpeed = baseSpeed + speedBonus;
        
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeDamage", newDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeAtkSpeed", newSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
    }

    /**
     * Применяет бонусы к БРОНЕ, суммируя их с ванильными.
     */
    private void applyArmorBonus(ItemMeta meta, Material type, EquipmentSlot slot, double armorBonus, double healthBonus, double toughnessBonus, double knockbackBonus) {
        
        // (Этот метод не требует проверки на медь, т.к. мы улучшаем только кирку)
        double baseArmor = getVanillaAttribute(type, Attribute.GENERIC_ARMOR, false);
        double newArmor = baseArmor + armorBonus;
        
        double baseToughness = getVanillaAttribute(type, Attribute.GENERIC_ARMOR_TOUGHNESS, false);
        double newToughness = baseToughness + toughnessBonus;

        double baseKnockback = getVanillaAttribute(type, Attribute.GENERIC_KNOCKBACK_RESISTANCE, false);
        double newKnockback = baseKnockback + knockbackBonus;

        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
        meta.removeAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeArmor", newArmor, AttributeModifier.Operation.ADD_NUMBER, slot));
        
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeToughness", newToughness, AttributeModifier.Operation.ADD_NUMBER, slot));
        
        meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeKnockback", newKnockback, AttributeModifier.Operation.ADD_NUMBER, slot));
        
        if (healthBonus > 0) {
            meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, 
                new AttributeModifier(UUID.randomUUID(), "UpgradeHealth", healthBonus, AttributeModifier.Operation.ADD_NUMBER, slot));
        }
    }

    /**
     * "Жестко закодированная" база данных ванильных статов.
     * ИСПРАВЛЕНО: Удалены все "ванильные" ссылки на медь, но добавлена проверка на
     * isCopperPick (по display name)
     */
    private double getVanillaAttribute(Material type, Attribute attribute, boolean isCopperPick) {
        String name = type.name();

        if (attribute == Attribute.GENERIC_ATTACK_DAMAGE) {
            // Урон (Модификатор. База игрока +1.0)
            if (isCopperPick) return 2.0; // Из вашего скриншота: 3 урона = 1 (база) + 2 (мод)

            if (name.endsWith("_SWORD")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 7.0; 
                if (name.startsWith("IRON_")) return 6.0; 
                if (name.startsWith("STONE_")) return 5.0; 
                if (name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) return 4.0; 
            } else if (name.endsWith("_AXE")) {
                 if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_") || name.startsWith("IRON_") || name.startsWith("STONE_")) return 9.0; 
                 if (name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) return 7.0; 
            } else if (name.endsWith("_PICKAXE")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 5.0; 
                if (name.startsWith("IRON_")) return 4.0; 
                if (name.startsWith("STONE_")) return 3.0; 
                if (name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) return 2.0; 
            } 
            return 0;
        }

        if (attribute == Attribute.GENERIC_ATTACK_SPEED) {
            // Скорость (Модификатор. База 4.0)
            if (isCopperPick) return -2.8; // Из вашего скриншота: 1.2 скорости = 4.0 (база) - 2.8 (мод)
            
            if (name.endsWith("_SWORD")) return -2.4; // 1.6
            if (name.endsWith("_AXE")) {
                 if (name.startsWith("STONE_") || name.startsWith("WOODEN_")) return -3.2; // 0.8
                 if (name.startsWith("GOLDEN_")) return -3.0; // 1.0
                 return -3.1; // 0.9 (Iron, Diamond, Netherite)
            }
            if (name.endsWith("_PICKAXE")) return -2.8; // 1.2
            if (name.endsWith("_SHOVEL")) return -3.0; // 1.0
            return 0; 
        }

        if (attribute == Attribute.GENERIC_ARMOR) {
            // Броня
            if (name.endsWith("_HELMET")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 3.0;
                if (name.startsWith("IRON_") || name.startsWith("CHAINMAIL_") || name.startsWith("GOLDEN_")) return 2.0; 
                if (name.startsWith("LEATHER_")) return 1.0;
            } else if (name.endsWith("_CHESTPLATE")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 8.0;
                if (name.startsWith("IRON_")) return 6.0;
                if (name.startsWith("CHAINMAIL_") || name.startsWith("GOLDEN_")) return 5.0; 
                if (name.startsWith("LEATHER_")) return 3.0;
            } else if (name.endsWith("_LEGGINGS")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 6.0;
                if (name.startsWith("IRON_")) return 5.0;
                if (name.startsWith("CHAINMAIL_")) return 4.0; 
                if (name.startsWith("GOLDEN_")) return 3.0;
                if (name.startsWith("LEATHER_")) return 2.0;
            } else if (name.endsWith("_BOOTS")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 3.0;
                if (name.startsWith("IRON_")) return 2.0;
                if (name.startsWith("CHAINMAIL_") || name.startsWith("GOLDEN_") || name.startsWith("LEATHER_")) return 1.0; 
            }
            return 0;
        }

        if (attribute == Attribute.GENERIC_ARMOR_TOUGHNESS) {
            if (name.startsWith("NETHERITE_")) return 3.0;
            if (name.startsWith("DIAMOND_")) return 2.0;
            return 0;
        }

        if (attribute == Attribute.GENERIC_KNOCKBACK_RESISTANCE) {
            if (name.startsWith("NETHERITE_")) return 0.1; // 10%
            return 0;
        }

        return 0; // По умолчанию
    }

    private void applyDurability(ItemMeta meta, int level) {
        int currentLevel = meta.getEnchantLevel(Enchantment.EFFICIENCY); // ИСПРАВЛЕНО: Проверяем тоже EFFICIENCY
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
