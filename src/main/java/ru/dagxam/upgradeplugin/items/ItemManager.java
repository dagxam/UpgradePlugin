// ИЗМЕНЕНО
package ru.dagxam.upgradeplugin.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
// ИЗМЕНЕНО
import ru.dagxam.upgradeplugin.UpgradePlugin; // Импортируем главный класс

import java.util.Arrays;

public class ItemManager {

    // ИЗМЕНЕНО: Создаем ключ, привязанный к вашему плагину
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
            // Используем наш обновленный ключ
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
        // Используем наш обновленный ключ
        return container.has(UPGRADE_BOOK_KEY, PersistentDataType.STRING);
    }
}
