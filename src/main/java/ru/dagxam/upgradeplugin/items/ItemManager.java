package ru.dagxam.upgradeplugin.items;

import org.bukkit.ChatColor; // <-- НУЖЕН ЭТОТ ИМПОРТ
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
    
    // --- НОВЫЙ КЛЮЧ ДЛЯ ПРОВЕРКИ УЛУЧШЕНИЯ ---
    // Это "скрытая" метка, которую мы будем ставить на предмет
    public static final NamespacedKey UPGRADED_ITEM_KEY = new NamespacedKey("upgradeplugin", "upgraded_item");
    // ----------------------------------------

    private static final String UPGRADED_LORE_STRING = "§b[Улучшено]";

    @SuppressWarnings("deprecation") // Подавляем устаревший setDisplayName
    public static ItemStack createUpgradeBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            // Используем старый API (String), он надежно работает на Paper
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
     * ИСПРАВЛЕНО: Теперь проверяет NBT-тег, а не лор.
     * Это 100% надежный способ.
     */
    public static boolean isUpgraded(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        // Проверяем, есть ли у предмета наша "скрытая" метка
        return container.has(UPGRADED_ITEM_KEY, PersistentDataType.STRING);
    }
    
    /**
     * ИСПРАВЛЕНО: Используем старый API (getDisplayName)
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
