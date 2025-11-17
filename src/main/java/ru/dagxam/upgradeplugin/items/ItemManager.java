package ru.dagxam.upgradeplugin.items;

import net.kyori.adventure.text.Component; // <-- НУЖЕН ИМПОРТ
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer; // <-- НУЖЕН ИМПОРТ
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
    private static final PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();

    public static ItemStack createUpgradeBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§bКнига Улучшения")); // Используем Paper Component
            meta.lore(Arrays.asList(
                Component.text("§7Используйте на наковальне"),
                Component.text("§7вместе с предметом для его"),
                Component.text("§7улучшения.")
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
     * Проверяет, улучшен ли предмет (имеет ли он лор "[Улучшено]").
     */
    public static boolean isUpgraded(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        
        List<Component> lore = meta.lore(); // Используем Paper API
        
        if (lore == null) {
            return false;
        }
        
        // §b - это бирюзовый цвет, который мы добавили в AnvilListener
        Component upgradedLore = Component.text("§b[Улучшено]");
        return lore.contains(upgradedLore);
    }
    
    /**
     * НОВЫЙ МЕТОД: Проверяет, является ли предмет медным инструментом (по имени)
     */
    public static boolean isCopperTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        Component nameComponent = item.displayName();
        String lowerName = plainTextSerializer.serialize(nameComponent).trim().toLowerCase();
        
        return lowerName.startsWith("медная кирка") || lowerName.startsWith("copper pickaxe") ||
               lowerName.startsWith("медный топор") || lowerName.startsWith("copper axe") ||
               lowerName.startsWith("медная лопата") || lowerName.startsWith("copper shovel") ||
               lowerName.startsWith("медная мотыга") || lowerName.startsWith("copper hoe") ||
               lowerName.startsWith("медный меч") || lowerName.startsWith("copper sword");
    }
}
