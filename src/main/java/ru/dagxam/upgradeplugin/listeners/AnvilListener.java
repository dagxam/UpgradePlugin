package ru.dagxam.upgradeplugin.listeners;

import com.google.common.collect.Multimap;
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
        
        String displayName = meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : "";

        // --- ЛОГИКА УЛУЧШЕНИЯ ---

        // --- ПРОВЕРКА МЕДНЫХ ПРЕДМЕТОВ (ПО НАЗВАНИЮ) ---
        // (Медная броня по вашему запросу)
        if (displayName.equalsIgnoreCase("Медная кираса") || displayName.equalsIgnoreCase("Copper Chestplate") ||
            displayName.equalsIgnoreCase("Медный шлем") || displayName.equalsIgnoreCase("Copper Helmet") ||
            displayName.equalsIgnoreCase("Медные поножи") || displayName.equalsIgnoreCase("Copper Leggings") ||
            displayName.equalsIgnoreCase("Медные ботинки") || displayName.equalsIgnoreCase("Copper Boots")) 
        {
            // +3 брони, +3 сердца (6.0)
            applyArmorBonus(meta, type, getSlot(type), 3.0, 6.0, 0, 0, displayName); 
            applyDurability(meta, 3);
            success = true;
        }
        // (Медные инструменты по вашему запросу)
        else if (displayName.equalsIgnoreCase("Медная кирка") || displayName.equalsIgnoreCase("Copper Pickaxe") ||
                 displayName.equalsIgnoreCase("Медный топор") || displayName.equalsIgnoreCase("Copper Axe") ||
                 displayName.equalsIgnoreCase("Медная лопата") || displayName.equalsIgnoreCase("Copper Shovel") ||
                 displayName.equalsIgnoreCase("Медная мотыга") || displayName.equalsIgnoreCase("Copper Hoe")) 
        {
            // +6 урон, +6 скорость
            applyWeaponBonus(meta, type, 6.0, 6.0, displayName); 
            if (displayName.equalsIgnoreCase("Медная кирка") || displayName.equalsIgnoreCase("Copper Pickaxe")) {
                meta.addEnchant(Enchantment.EFFICIENCY, 12, true); // "8 ударов"
            }
            success = true;
        }
        // Кожа
        else if (type.name().startsWith("LEATHER_")) {
            applyArmorBonus(meta, type, getSlot(type), 2.0, 4.0, 0, 0, displayName); 
            applyDurability(meta, 2);
            success = true;
        }
        // Кольчуга
        else if (type.name().startsWith("CHAINMAIL_")) {
            applyArmorBonus(meta, type, getSlot(type), 4.0, 8.0, 0, 0, displayName); 
            applyDurability(meta, 4);
            success = true;
        }
        // Железо
        else if (type.name().startsWith("IRON_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 6.0, 12.0, 0, 0, displayName); 
                applyDurability(meta, 6);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 8.0, 8.0, displayName);
                if (type == Material.IRON_PICKAXE) {
                    meta.addEnchant(Enchantment.EFFICIENCY, 15, true); 
                }
                success = true;
            }
        }
        // Золото
        else if (type.name().startsWith("GOLDEN_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 7.0, 14.0, 0, 0, displayName);
                applyDurability(meta, 7);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 9.0, 9.0, displayName);
                if (type == Material.GOLDEN_PICKAXE) {
                    meta.addEnchant(Enchantment.EFFICIENCY, 12, true);
                }
                success = true;
            }
        }
        // Алмазы
        else if (type.name().startsWith("DIAMOND_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 10.0, 20.0, 10.0, 0, displayName); 
                applyDurability(meta, 10);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 12.0, 12.0, displayName);
                if (type == Material.DIAMOND_PICKAXE) {
                    meta.addEnchant(Enchantment.EFFICIENCY, 20, true);
                }
                success = true;
            }
        }
        // Незерит
        else if (type.name().startsWith("NETHERITE_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(meta, type, getSlot(type), 15.0, 30.0, 15.0, 1.5, displayName); 
                applyDurability(meta, 15);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(meta, type, 15.0, 15.0, displayName);
                if (type == Material.NETHERITE_PICKAXE) {
                    meta.addEnchant(Enchantment.EFFICIENCY, 25, true);
                }
                success = true;
            }
        }
        // Дерево
        else if (type.name().startsWith("WOODEN_")) {
            applyWeaponBonus(meta, type, 2.0, 2.0, displayName);
            if (type == Material.WOODEN_PICKAXE) {
                meta.addEnchant(Enchantment.EFFICIENCY, 8, true);
            }
            success = true;
        }
        // Камень
        else if (type.name().startsWith("STONE_")) {
            applyWeaponBonus(meta, type, 4.0, 4.0, displayName);
            if (type == Material.STONE_PICKAXE) {
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
    private void applyWeaponBonus(ItemMeta meta, Material type, double damageBonus, double speedBonus, String displayName) {
        
        double baseDamage = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_DAMAGE, displayName);
        double newDamage = baseDamage + damageBonus;
        
        double baseSpeed = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_SPEED, displayName);
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
    private void applyArmorBonus(ItemMeta meta, Material type, EquipmentSlot slot, double armorBonus, double healthBonus, double toughnessBonus, double knockbackBonus, String displayName) {
        
        double baseArmor = getVanillaAttribute(type, Attribute.GENERIC_ARMOR, displayName);
        double newArmor = baseArmor + armorBonus;
        
        double baseToughness = getVanillaAttribute(type, Attribute.GENERIC_ARMOR_TOUGHNESS, displayName);
        double newToughness = baseToughness + toughnessBonus;

        double baseKnockback = getVanillaAttribute(type, Attribute.GENERIC_KNOCKBACK_RESISTANCE, displayName);
        double newKnockback = baseKnockback + knockbackBonus;

        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
        meta.
