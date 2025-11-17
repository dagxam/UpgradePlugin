package ru.dagxam.upgradeplugin.items;

import net.kyori.adventure.text.Component; 
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer; 
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
    // Константа для лора (чтобы не было опечаток)
    private static final Component UPGRADED_LORE = Component.text("§b[Улучшено]");

    public static ItemStack createUpgradeBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            // ИСПРАВЛЕНО: Используем Component
            meta.displayName(Component.text("§bКнига Улучшения")); 
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
     * ИСПРАВЛЕНО: Проверяет Component лор
     */
    public static boolean isUpgraded(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        
        // ИСПРАВЛЕНО: Используем .lore() (Paper API)
        List<Component> lore = meta.lore(); 
        
        if (lore == null) {
            return false;
        }
        
        // Проверяем Component
        return lore.contains(UPGRADED_LORE);
    }
    
    /**
     * Проверяет, является ли предмет медным инструментом (по имени)
     */
    public static boolean isCopperTool(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        Component nameComponent = item.displayName(); // Безопасно для Paper
        String lowerName = plainTextSerializer.serialize(nameComponent).trim().toLowerCase();
        
        return lowerName.startsWith("медная кирка") || lowerName.startsWith("copper pickaxe") ||
               lowerName.startsWith("медный топор") || lowerName.startsWith("copper axe") ||
               lowerName.startsWith("медная лопата") || lowerName.startsWith("copper shovel") ||
               lowerName.startsWith("медная мотыга") || lowerName.startsWith("copper hoe") ||
               lowerName.startsWith("медный меч") || lowerName.startsWith("copper sword");
    }
}
