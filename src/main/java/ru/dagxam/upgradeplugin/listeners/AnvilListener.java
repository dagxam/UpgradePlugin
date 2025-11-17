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

        List<String> lore = resultMeta.hasLore() ? new ArrayList<>(resultMeta.getLegacyLore()) : new ArrayList<>(); // Используем getLegacyLore для String
        if (lore.contains("§b[Улучшено]")) {
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
            // Мы НЕ добавляем эффективность медной кирке, т.к. будем контролировать ее скорость в BlockBreakListener
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
                // ИЗМЕНЕНО: Не добавляем эффективность
                success = true;
            }
        }
