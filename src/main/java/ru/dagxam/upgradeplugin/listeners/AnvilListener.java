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
            // +2 брони, +2 сердца (4.0)
            applyArmorBonus(meta, getSlot(type), 2.0, 4.0, 0, 0); 
            applyDurability(meta, 2);
            success = true;
        }
        // Кольчуга
        else if (type.name().startsWith("CHAINMAIL_")) {
            // +4 брони, +4 сердца (8.0)
            applyArmorBonus(meta, getSlot(type), 4.0, 8.0, 0, 0); 
            applyDurability(meta, 4);
            success = true;
        }
        // Железо
        else if (type.name().startsWith("IRON_")) {
            if (getSlot(type) != null) { // Броня
                // +6 брони, +6 сердец (12.0)
                applyArmorBonus(meta, getSlot(type), 6.0, 12.0, 0, 0); 
                applyDurability(meta, 6);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, 8.0, 8.0); // +8 урон, +8 скорость
                success = true;
            }
        }
        // Золото
        else if (type.name().startsWith("GOLDEN_")) {
            if (getSlot(type) != null) { // Броня
                // +7 брони, +7 сердец (14.0)
                applyArmorBonus(meta, getSlot(type), 7.0, 14.0, 0, 0);
                applyDurability(meta, 7);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, 9.0, 9.0); // +9 урон, +9 скорость
                success = true;
            }
        }
        // Алмазы
        else if (type.name().startsWith("DIAMOND_")) {
            if (getSlot(type) != null) { // Броня
                // +10 брони, +10 сердец (20.0), +10 твердости
                applyArmorBonus(meta, getSlot(type), 10.0, 20.0, 10.0, 0); 
                applyDurability(meta, 10);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, 12.0, 12.0); // +12 урон, +12 скорость
                success = true;
            }
        }
        // Незерит
        else if (type.name().startsWith("NETHERITE_")) {
            if (getSlot(type) != null) { // Броня
                // +15 брони, +15 сердец (30.0), +15 твердости, +1.5 (150%) сопр. отбрасыванию
                applyArmorBonus(meta, getSlot(type), 15.0, 30.0, 15.0, 1.5); 
                applyDurability(meta, 15);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, 15.0, 15.0); // +15 урон, +15 скорость
                success = true;
            }
        }
        // Дерево
        else if (type.name().startsWith("WOODEN_")) {
            applyWeaponBonus(meta, 2.0, 2.0);
            success = true;
        }
        // Камень
        else if (type.name().startsWith("STONE_")) {
            applyWeaponBonus(meta, 4.0, 4.0);
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
     * ПРАВИЛЬНЫЙ метод для добавления бонусов ОРУЖИЮ и ИНСТРУМЕНТАМ.
     * Он находит существующие бонусы, добавляет к ним наши значения и заменяет их.
     */
    private void applyWeaponBonus(ItemMeta meta, double damageBonus, double speedBonus) {
        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
        
        // 1. Рассчитываем новый УРОН
        // Урон = (База игрока 1.0) + (Модификатор)
        double newDamage = damageBonus; // Наш бонус
        if (modifiers != null && modifiers.containsKey(Attribute.GENERIC_ATTACK_DAMAGE)) {
            for (AttributeModifier mod : modifiers.get(Attribute.GENERIC_ATTACK_DAMAGE)) {
                newDamage += mod.getAmount(); // Добавляем ванильный урон (например, +7 у незерита)
            }
        }
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeDamage", newDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        // 2. Рассчитываем новую СКОРОСТЬ АТАКИ
        // Скорость = (База 4.0) + (Модификатор)
        double newSpeed = speedBonus; // Наш бонус
        if (modifiers != null && modifiers.containsKey(Attribute.GENERIC_ATTACK_SPEED)) {
             for (AttributeModifier mod : modifiers.get(Attribute.GENERIC_ATTACK_SPEED)) {
                newSpeed += mod.getAmount(); // Добавляем ванильный модификатор (например, -2.4)
            }
        }
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
         meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeAtkSpeed", newSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
    }

    /**
     * ИСПРАВЛЕННЫЙ метод для добавления бонусов БРОНЕ.
     * Теперь он тоже суммирует броню, твердость и сопр. отбрасыванию.
     */
    private void applyArmorBonus(ItemMeta meta, EquipmentSlot slot, double armorBonus, double healthBonus, double toughnessBonus, double knockbackBonus) {
        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();

        // 1. Рассчитываем новую БРОНЮ
        double newArmor = armorBonus;
        if (modifiers != null && modifiers.containsKey(Attribute.GENERIC_ARMOR)) {
            for (AttributeModifier mod : modifiers.get(Attribute.GENERIC_ARMOR)) {
                newArmor += mod.getAmount(); // Добавляем ванильное значение
            }
        }
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
        if (newArmor != 0) // Не добавляем, если в итоге 0
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR, 
                new AttributeModifier(UUID.randomUUID(), "UpgradeArmor", newArmor, AttributeModifier.Operation.ADD_NUMBER, slot));

        // 2. Рассчитываем новую ТВЕРДОСТЬ БРОНИ
        double newToughness = toughnessBonus;
        if (modifiers != null && modifiers.containsKey(Attribute.GENERIC_ARMOR_TOUGHNESS)) {
            for (AttributeModifier mod : modifiers.get(Attribute.GENERIC_ARMOR_TOUGHNESS)) {
                newToughness += mod.getAmount(); // Добавляем ванильное значение
            }
        }
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
        if (newToughness != 0)
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, 
                new AttributeModifier(UUID.randomUUID(), "UpgradeToughness", newToughness, AttributeModifier.Operation.ADD_NUMBER, slot));

        // 3. Рассчитываем новое СОПРОТИВЛЕНИЕ ОТБРАСЫВАНИЮ
        double newKnockback = knockbackBonus;
         if (modifiers != null && modifiers.containsKey(Attribute.GENERIC_KNOCKBACK_RESISTANCE)) {
            for (AttributeModifier mod : modifiers.get(Attribute.GENERIC_KNOCKBACK_RESISTANCE)) {
                newKnockback += mod.getAmount(); // Добавляем ванильное значение
            }
        }
        meta.removeAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (newKnockback != 0)
            meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 
                new AttributeModifier(UUID.randomUUID(), "UpgradeKnockback", newKnockback, AttributeModifier.Operation.ADD_NUMBER, slot));


        // 4. ЗДОРОВЬЕ - всегда просто добавляется поверх всего, не заменяется
        if (healthBonus > 0) {
            meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, 
                new AttributeModifier(UUID.randomUUID(), "UpgradeHealth", healthBonus, AttributeModifier.Operation.ADD_NUMBER, slot));
        }
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
