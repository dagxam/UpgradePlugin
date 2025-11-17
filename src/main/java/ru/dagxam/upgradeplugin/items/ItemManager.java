package ru.dagxam.upgradeplugin.items;

import org.bukkit.ChatColor; 
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class ItemManager {

    public static final NamespacedKey UPGRADE_BOOK_KEY = new NamespacedKey("upgradeplugin", "upgrade_book");
    
    // ИСПОЛЬЗУЕМ СТРОКУ
    private static final String UPGRADED_LORE_STRING = "§b[Улучшено]";
    // ИСПРАВЛЕНО: Текст, который мы ищем (БЕЗ ЦВЕТА)
    private static final String LORE_CHECK_STRING = "[Улучшено]";

    @SuppressWarnings("deprecation") // Подавляем устаревший setDisplayName
    public static ItemStack createUpgradeBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§bКнига Улучшения"); 
            meta.setLore(Arrays.asList(
                "§7Используйте на наковальне",
                "§7вместе с предметом для его",
                "§7улучшения."
            ));

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(UPGRADE_BOOK_KEY, PersistentDataType.STRING, "true");

            book.setItemMeta(meta);
        }
        return book;
    }

    public static boolean isUpgradeBook(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(UPGRADE_BOOK_KEY, PersistentDataType.STRING);
    }

    /**
     * ИСПРАВЛЕНО: Проверяет String лор, ИГНОРИРУЯ ЦВЕТА
     */
    @SuppressWarnings("deprecation") // Подавляем устаревший getLore
    public static boolean isUpgraded(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        
        List<String> lore = meta.getLore(); 
        
        if (lore == null) {
            return false;
        }
        
        // ИСПРАВЛЕННАЯ ЛОГИКА
        for (String line : lore) {
            // Убираем все коды цвета ("§b", "§f" и т.д.) из строки
            String strippedLine = ChatColor.stripColor(line);
            
            // Проверяем, содержит ли "чистая" строка наш текст
            if (strippedLine.contains(LORE_CHECK_STRING)) {
                return true; // Найдено!
            }
        }
        
        return false; // Не найдено
    }
    
    /**
     * Проверяет, является ли предмет медным инструментом (по имени)
     */
    @SuppressWarnings("deprecation") 
    public static boolean isCopperTool(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        String lowerName = ChatColor.stripColor(item.getItemMeta().getDisplayName().toLowerCase());
        
        return lowerName.startsWith("медная кирка") || lowerName.startsWith("copper pickaxe") ||
               lowerName.startsWith("медный топор") || lowerName.startsWith("copper axe") ||
               lowerName.startsWith("медная лопата") || lowerName.startsWith("copper shovel") ||
               lowerName.startsWith("медная мотыга") || lowerName.startsWith("copper hoe") ||
               lowerName.startsWith("медный меч") || lowerName.startsWith("copper sword");
    }
}
