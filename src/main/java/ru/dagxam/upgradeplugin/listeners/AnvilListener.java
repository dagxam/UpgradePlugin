package ru.dagxam.upgradeplugin.listeners;

import com.google.common.collect.Multimap;
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

        // --- ЛОГИКА УЛУЧШЕНИЯ ---

        // Кожа
        if (type.name().startsWith("LEATHER_")) {
            applyArmorBonus(meta, type, getSlot(type), 2.0, 4.0, 0, 0); // +2 брони, +2 сердца (4.0)
            applyDurability(meta, 2);
            success = true;
        }
        // Кольчуга
        else if (type.name().startsWith("CHAINMAIL_")) {
            applyArmorBonus(meta, type, getSlot(type), 4.0, 8.0, 0, 0); // +4 брони, +4 сердца (8.0)
            applyDurability(meta, 4);
            success = true;
        }
        // Железо
        else if (type.name().startsWith("IRON_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 6.0, 12.0, 0, 0); // +6 брони, +6 сердец (12.0)
                applyDurability(meta, 6);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 8.0, 8.0); // +8 урон, +8 скорость
                success = true;
            }
        }
        // Золото
        else if (type.name().startsWith("GOLDEN_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 7.0, 14.0, 0, 0); // +7 брони, +7 сердец (14.0)
                applyDurability(meta, 7);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 9.0, 9.0); // +9 урон, +9 скорость
                success = true;
            }
        }
        // Алмазы
        else if (type.name().startsWith("DIAMOND_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 10.0, 20.0, 10.0, 0); // +10 брони, +10 сердец (20.0), +10 твердости
                applyDurability(meta, 10);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 12.0, 12.0); // +12 урон, +12 скорость
                success = true;
            }
        }
        // Незерит
        else if (type.name().startsWith("NETHERITE_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 15.0, 30.0, 15.0, 1.5); // +15 всего
                applyDurability(meta, 15);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 15.0, 15.0); // +15 урон, +15 скорость
                success = true;
            }
        }
        // Дерево
        else if (type.name().startsWith("WOODEN_")) {
            applyWeaponBonus(meta, type, 2.0, 2.0);
            success = true;
        }
        // Камень
        else if (type.name().startsWith("STONE_")) {
            applyWeaponBonus(meta, type, 4.0, 4.0);
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
        // 1. Урон при Атаке
        double baseDamage = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_DAMAGE);
        double newDamage = baseDamage + damageBonus;
        
        // 2. Скорость Атаки
        double baseSpeed = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_SPEED);
        double newSpeed = baseSpeed + speedBonus;
        
        // Удаляем ВСЕ старые модификаторы (включая неявные, этот вызов их отключает)
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);

        // Добавляем новые, уже рассчитанные
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeDamage", newDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeAtkSpeed", newSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
    }

    /**
     * Применяет бонусы к БРОНЕ, суммируя их с ванильными.
     */
    private void applyArmorBonus(ItemMeta meta, Material type, EquipmentSlot slot, double armorBonus, double healthBonus, double toughnessBonus, double knockbackBonus) {
        
        // 1. Броня
        double baseArmor = getVanillaAttribute(type, Attribute.GENERIC_ARMOR);
        double newArmor = baseArmor + armorBonus;
        
        // 2. Твердость
        double baseToughness = getVanillaAttribute(type, Attribute.GENERIC_ARMOR_TOUGHNESS);
        double newToughness = baseToughness + toughnessBonus;

        // 3. Сопр. Отбрасыванию
        double baseKnockback = getVanillaAttribute(type, Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        double newKnockback = baseKnockback + knockbackBonus;

        // Удаляем старые
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
        meta.removeAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        
        // Добавляем новые
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeArmor", newArmor, AttributeModifier.Operation.ADD_NUMBER, slot));
        
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeToughness", newToughness, AttributeModifier.Operation.ADD_NUMBER, slot));
        
        meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeKnockback", newKnockback, AttributeModifier.Operation.ADD_NUMBER, slot));
        
        // 4. Здоровье - всегда просто бонус, не суммируется
        if (healthBonus > 0) {
            meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, 
                new AttributeModifier(UUID.randomUUID(), "UpgradeHealth", healthBonus, AttributeModifier.Operation.ADD_NUMBER, slot));
        }
    }

    /**
     * "Жестко закодированная" база данных ванильных статов.
     * Это единственный надежный способ "прочитать" неявные атрибуты.
     */
    private double getVanillaAttribute(Material type, Attribute attribute) {
        String name = type.name();

        if (attribute == Attribute.GENERIC_ATTACK_DAMAGE) {
            // Урон (Модификатор. База игрока +1.0)
            if (name.endsWith("_SWORD")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 7.0; // 1+7 = 8
                if (name.startsWith("IRON_")) return 6.0; // 1+6 = 7
                if (name.startsWith("STONE_")) return 5.0; // 1+5 = 6
                if (name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) return 4.0; // 1+4 = 5
            } else if (name.endsWith("_AXE")) {
                if (name.startsWith("NETHERITE_")) return 9.0; // 1+9 = 10
                if (name.startsWith("DIAMOND_") || name.startsWith("IRON_") || name.startsWith("STONE_")) return 8.0; // 1+8 = 9
                if (name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) return 6.0; // 1+6 = 7
            } else if (name.endsWith("_PICKAXE")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 5.0; // 1+5 = 6
                if (name.startsWith("IRON_")) return 4.0; // 1+4 = 5
                if (name.startsWith("STONE_")) return 3.0; // 1+3 = 4
                if (name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) return 2.0; // 1+2 = 3
            } //... и т.д. для лопат и мотыг, если нужно
            return 0; // Ванильный урон 0 (база 1.0)
        }

        if (attribute == Attribute.GENERIC_ATTACK_SPEED) {
            // Скорость (Модификатор. База 4.0)
            if (name.endsWith("_SWORD")) return -2.4; // 4.0 - 2.4 = 1.6
            if (name.endsWith("_AXE")) {
                if (name.startsWith("STONE_") || name.startsWith("WOODEN_")) return -3.2; // 4.0 - 3.2 = 0.8
                return -3.0; // 4.0 - 3.0 = 1.0 (для Iron, Gold, Diamond, Netherite)
            }
            if (name.endsWith("_PICKAXE")) return -2.8; // 4.0 - 2.8 = 1.2
            if (name.endsWith("_SHOVEL")) return -3.0; // 4.0 - 3.0 = 1.0
            return 0; // Ванильная скорость 0 (база 4.0)
        }

        if (attribute == Attribute.GENERIC_ARMOR) {
            // Броня
            if (name.endsWith("_HELMET")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_") || name.startsWith("LEATHER_")) return 3.0;
                if (name.startsWith("IRON_") || name.startsWith("GOLDEN_") || name.startsWith("CHAINMAIL_")) return 2.0;
            } else if (name.endsWith("_CHESTPLATE")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 8.0;
                if (name.startsWith("IRON_")) return 6.0;
                if (name.startsWith("GOLDEN_") || name.startsWith("CHAINMAIL_")) return 5.0;
                if (name.startsWith("LEATHER_")) return 3.0;
            } else if (name.endsWith("_LEGGINGS")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_")) return 6.0;
                if (name.startsWith("IRON_")) return 5.0;
                if (name.startsWith("GOLDEN_") || name.startsWith("CHAINMAIL_")) return 4.0;
                if (name.startsWith("LEATHER_")) return 2.0;
            } else if (name.endsWith("_BOOTS")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_") || name.startsWith("LEATHER_")) return 3.0;
                if (name.startsWith("IRON_") || name.startsWith("GOLDEN_") || name.startsWith("CHAINMAIL_")) return 2.0;
            }
            return 0;
        }

        if (attribute == Attribute.GENERIC_ARMOR_TOUGHNESS) {
            // Твердость
            if (name.startsWith("NETHERITE_")) return 3.0;
            if (name.startsWith("DIAMOND_")) return 2.0;
            return 0;
        }

        if (attribute == Attribute.GENERIC_KNOCKBACK_RESISTANCE) {
            // Сопр. Отбрасыванию
            if (name.startsWith("NETHERITE_")) return 0.1; // 10%
            return 0;
        }

        return 0; // По умолчанию
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
