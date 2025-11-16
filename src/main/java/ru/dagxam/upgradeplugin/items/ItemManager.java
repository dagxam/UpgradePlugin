package ru.dagxam.upgradeplugin.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List; // <-- УБЕДИТЕСЬ, ЧТО ЭТОТ ИМПОРТ ЕСТЬ

public class ItemManager {

    public static final NamespacedKey UPGRADE_BOOK_KEY = new NamespacedKey("upgradeplugin", "upgrade_book");

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

    // --- ДОБАВЬТЕ ЭТОТ НОВЫЙ МЕТОД ---
    /**
     * Проверяет, улучшен ли предмет (имеет ли он лор "[Улучшено]").
     */
    public static boolean isUpgraded(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        
        // Используем устаревший, но простой и рабочий метод getLore()
        @SuppressWarnings("deprecation")
        List<String> lore = meta.getLore(); 
        
        if (lore == null) {
            return false;
        }
        
        // §b - это бирюзовый цвет, который мы добавили в AnvilListener
        return lore.contains("§b[Улучшено]");
    }
    // --- КОНЕЦ НОВОГО МЕТОДА ---
}
