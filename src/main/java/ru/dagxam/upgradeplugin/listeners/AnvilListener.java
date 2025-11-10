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
        
        // Проверка, улучшен ли предмет (чтобы не улучшать дважды)
        if (lore.contains("§b[Улучшено]")) {
             event.setResult(null); // Запрещаем улучшение
             return;
        }

        Material type = resultItem.getType();
        boolean
