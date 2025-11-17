package ru.dagxam.upgradeplugin.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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
    private final PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();
    
    // ИСПРАВЛЕНО: Создаем константу для лора, чтобы использовать ее везде
    private static final Component UPGRADED_LORE = Component.text("§b[Улучшено]");

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

        ItemMeta meta = firstItem.getItemMeta(); 
        if (meta == null) {
            return;
        }
        
        ItemStack resultItem = firstItem.clone();
        ItemMeta resultMeta = resultItem.getItemMeta(); 

        // ИСПРАВЛЕНО: Используем .lore() (Paper API) для получения Списка Компонентов
        List<Component> lore = resultMeta.hasLore() ? new ArrayList<>(resultMeta.lore()) : new ArrayList<>();
        
        // ИСПРАВЛЕНО: Проверяем наличие Компонента
        if (lore.contains(UPGRADED_LORE)) { 
            event.setResult(null);
            return;
        }

        Material type = resultItem.getType();
        boolean success = false;
        
        Component nameComponent;
        if (meta.hasDisplayName()) {
            nameComponent = meta.displayName(); 
        } else {
            nameComponent = Component.translatable(type.translationKey()); 
        }
        String displayName = plainTextSerializer.serialize(nameComponent).trim();
        String lowerName = displayName.toLowerCase(); 

        // --- ЛОГИКА УЛУЧШЕНИЯ ---

        if (lowerName.startsWith("медная кираса") || lowerName.startsWith("copper chestplate") ||
            lowerName.startsWith("медный шлем") || lowerName.startsWith("copper helmet") ||
            lowerName.startsWith("медные поножи") || lowerName.startsWith("copper leggings") ||
            lowerName.startsWith("медные ботинки") || lowerName.startsWith("copper boots")) 
        {
            applyArmorBonus(resultMeta, type, getSlot(type), 3.0, 6.0, 0, 0, displayName); 
            applyDurability(resultMeta, 3);
            success = true;
        }
        else if (lowerName.startsWith("медная кирка") || lowerName.startsWith("copper pickaxe") ||
                 lowerName.startsWith("медный топор") || lowerName.startsWith("copper axe") ||
                 lowerName.startsWith("медная лопата") || lowerName.startsWith("copper shovel") ||
                 lowerName.startsWith("медная мотыга") || lowerName.startsWith("copper hoe") ||
                 lowerName.startsWith("медный меч") || lowerName.startsWith("copper sword")) 
        {
            applyWeaponBonus(resultMeta, type, 6.0, 6.0, displayName); 
            success = true;
        }
        // Кожа
        else if (type.name().startsWith("LEATHER_")) {
            applyArmorBonus(resultMeta, type, getSlot(type), 2.0, 4.0, 0, 0, displayName); 
            applyDurability(resultMeta, 2);
            success = true;
        }
        // Кольчуга
        else if (type.name().startsWith("CHAINMAIL_")) {
            applyArmorBonus(resultMeta, type, getSlot(type), 4.0, 8.0, 0, 0, displayName); 
            applyDurability(resultMeta, 4);
            success = true;
        }
        // Железо
        else if (type.name().startsWith("IRON_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(resultMeta, type, getSlot(type), 6.0, 12.0, 0, 0, displayName); 
                applyDurability(resultMeta, 6);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(resultMeta, type, 8.0, 8.0, displayName);
                success = true;
            }
        }
        // Золото
        else if (type.name().startsWith("GOLDEN_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(resultMeta, type, getSlot(type), 7.0, 14.0, 0, 0, displayName);
                applyDurability(resultMeta, 7);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(resultMeta, type, 9.0, 9.0, displayName);
                success = true;
            }
        }
        // Алмазы
        else if (type.name().startsWith("DIAMOND_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(resultMeta, type, getSlot(type), 10.0, 20.0, 10.0, 0, displayName); 
                applyDurability(resultMeta, 10);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(resultMeta, type, 12.0, 12.0, displayName);
                if (type == Material.DIAMOND_PICKAXE) {
                    resultMeta.addEnchant(Enchantment.EFFICIENCY, 20, true);
                }
                success = true;
            }
        }
        // Незерит
        else if (type.name().startsWith("NETHERITE_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(resultMeta, type, getSlot(type), 15.0, 30.0, 15.0, 1.5, displayName); 
                applyDurability(resultMeta, 15);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(resultMeta, type, 15.0, 15.0, displayName);
                if (type == Material.NETHERITE_PICKAXE) {
                    resultMeta.addEnchant(Enchantment.EFFICIENCY, 25, true);
                }
                success = true;
            }
        }
        // Дерево
        else if (type.name().startsWith("WOODEN_")) {
            applyWeaponBonus(resultMeta, type, 2.0, 2.0, displayName);
            if (type == Material.WOODEN_PICKAXE) {
                resultMeta.addEnchant(Enchantment.EFFICIENCY, 8, true);
            }
            success = true;
        }
        // Камень
        else if (type.name().startsWith("STONE_")) {
            applyWeaponBonus(resultMeta, type, 4.0, 4.0, displayName);
            if (type == Material.STONE_PICKAXE) {
                resultMeta.addEnchant(Enchantment.EFFICIENCY, 10, true);
            }
            success = true;
        }

        // --- КОНЕЦ ЛОГИКИ ---

        if (success) {
            // ИСПРАВЛЕНО: Добавляем Компонент
            lore.add(UPGRADED_LORE);
            // ИСПРАВЛЕНО: Устанавливаем Компонент
            resultMeta.lore(lore); 
            resultItem.setItemMeta(resultMeta); 
            event.setResult(resultItem);
            
            // Используем устаревший, но рабочий метод
            inventory.setRepairCost(20);
        }
    }

    // ... (Остальная часть файла: applyWeaponBonus, applyArmorBonus, getVanillaAttribute, applyDurability, getSlot) ...
    // ... (Они остаются БЕЗ ИЗМЕНЕНИЙ, как в предыдущем ответе) ...
    
    private void applyWeaponBonus(ItemMeta meta, Material type, double damageBonus, double speedBonus, String displayName) {
        
        double baseDamage = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_DAMAGE, displayName);
        double newDamage = baseDamage + damageBonus;
        
        double baseSpeed = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_SPEED, displayName);
        double newSpeed = baseSpeed + speedBonus;
        
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);

        AttributeModifier damageMod = new AttributeModifier(UUID.randomUUID(), "UpgradeDamage", newDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageMod);
        
        AttributeModifier speedMod = new AttributeModifier(UUID.randomUUID(), "UpgradeAtkSpeed", newSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedMod);
    }

    private void applyArmorBonus(ItemMeta meta, Material type, EquipmentSlot slot, double armorBonus, double healthBonus, double toughnessBonus, double knockbackBonus, String displayName) {
        
        double baseArmor = getVanillaAttribute(type, Attribute.GENERIC_ARMOR, displayName);
        double newArmor = baseArmor + armorBonus;
        
        double baseToughness = getVanillaAttribute(type, Attribute.GENERIC_ARMOR_TOUGHNESS, displayName); 
        double newToughness = baseToughness + toughnessBonus;

        double baseKnockback = getVanillaAttribute(type, Attribute.GENERIC_KNOCKBACK_RESISTANCE, displayName);
        double newKnockback = baseKnockback + knockbackBonus;

        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
        meta.removeAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        
        if (newArmor > 0) {
            AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), "UpgradeArmor", newArmor, AttributeModifier.Operation.ADD_NUMBER, slot);
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR, mod);
        }
        
        if (newToughness > 0) {
            AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), "UpgradeToughness", newToughness, AttributeModifier.Operation.ADD_NUMBER, slot);
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, mod);
        }
        
        if (newKnockback > 0) {
             AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), "UpgradeKnockback", newKnockback, AttributeModifier.Operation.ADD_NUMBER, slot);
            meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, mod);
        }
        
        if (healthBonus > 0) {
            AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), "UpgradeHealth", healthBonus, AttributeModifier.Operation.ADD_NUMBER, slot);
            meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, mod);
        }
    }

    private double getVanillaAttribute(Material type, Attribute attribute, String displayName) {
        String name = type.name();
        String lowerName = displayName.toLowerCase();

        // --- ПРОВЕРКА МЕДНЫХ ПРЕДМЕТОВ ПО ИМЕНИ ---
        if (attribute == Attribute.GENERIC_ATTACK_DAMAGE) {
            if (lowerName.startsWith("медная кирка") || lowerName.startsWith("copper pickaxe")) return 2.0; 
            if (lowerName.startsWith("медный топор") || lowerName.startsWith("copper axe")) return 7.0; 
            if (lowerName.startsWith("медная лопата") || lowerName.startsWith("copper shovel")) return 2.5; 
            if (lowerName.startsWith("медная мотыга") || lowerName.startsWith("copper hoe")) return 1.0; 
            if (lowerName.startsWith("медный меч") || lowerName.startsWith("copper sword")) return 4.0; 
        }
        if (attribute == Attribute.GENERIC_ATTACK_SPEED) {
            if (lowerName.startsWith("медная кирка") || lowerName.startsWith("copper pickaxe")) return -2.8; 
            if (lowerName.startsWith("медный топор") || lowerName.startsWith("copper axe")) return -3.2; 
            if (lowerName.startsWith("медная лопата") || lowerName.startsWith("copper shovel")) return -3.0; 
            if (lowerName.startsWith("медная мотыга") || lowerName.startsWith("copper hoe")) return 0.0; 
            if (lowerName.startsWith("медный меч") || lowerName.startsWith("copper sword")) return -2.4; 
        }
        if (attribute == Attribute.GENERIC_ARMOR) {
            if (lowerName.startsWith("медный шлем") || lowerName.startsWith("copper helmet")) return 2.0;
            if (lowerName.startsWith("медная кираса") || lowerName.startsWith("copper chestplate")) return 5.0;
            if (lowerName.startsWith("медные поножи") || lowerName.startsWith("copper leggings")) return 4.0;
            if (lowerName.startsWith("медные ботинки") || lowerName.startsWith("copper boots")) return 1.0;
        }
        // --- КОНЕЦ ПРОВЕРКИ МЕДИ ---
        
        // --- ПРОВЕРКА ВАНИЛЬНЫХ ПРЕДМЕТОВ ---
        if (attribute == Attribute.GENERIC_ATTACK_DAMAGE) {
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
            else if (name.endsWith("_SHOVEL")) {
                if (name.startsWith("NETHERITE_")) return 5.5; 
                if (name.startsWith("DIAMOND_")) return 4.5; 
                if (name.startsWith("IRON_")) return 3.5; 
                if (name.startsWith("STONE_")) return 2.5; 
                if (name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) return 1.5; 
            }
            else if (name.endsWith("_HOE")) {
                 return 1.0; 
            }
            return 0;
        }

        if (attribute == Attribute.GENERIC_ATTACK_SPEED) {
            if (name.endsWith("_SWORD")) return -2.4; // 1.6
            if (name.endsWith("_AXE")) {
                 if (name.startsWith("STONE_") || name.startsWith("WOODEN_")) return -3.2; // 0.8
                 if (name.startsWith("GOLDEN_")) return -3.0; // 1.0
                 return -3.1; // 0.9 (Iron, Diamond, Netherite)
            }
            if (name.endsWith("_PICKAXE")) return -2.8; // 1.2
            if (name.endsWith("_SHOVEL")) return -3.0; // 1.0
            
            if (name.endsWith("_HOE")) {
                if (name.startsWith("NETHERITE_") || name.startsWith("DIAMOND_") || name.startsWith("IRON_") || name.startsWith("STONE_") || name.startsWith("GOLDEN_") || name.startsWith("WOODEN_")) return 0.0; // 4.0
            }
            return 0; 
        }

        if (attribute == Attribute.GENERIC_ARMOR) {
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
