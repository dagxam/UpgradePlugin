package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.dagxam.upgradeplugin.UpgradePlugin;
import ru.dagxam.upgradeplugin.items.ItemManager;

import java.util.ArrayList;
import java.util.List;

public class AnvilListener implements Listener {

    private final UpgradePlugin plugin;

    // тот же самый маркер, который использует BlockBreakListener
    private static final String UPGRADED_LORE_STRING = "§b[Улучшено]";

    public AnvilListener(UpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0); // предмет
        ItemStack secondItem = inventory.getItem(1); // книга улучшения

        // Должен быть предмет + книга улучшения
        if (firstItem == null || secondItem == null || !ItemManager.isUpgradeBook(secondItem)) {
            return;
        }

        if (!firstItem.hasItemMeta()) {
            return;
        }

        ItemStack resultItem = firstItem.clone();
        ItemMeta meta = resultItem.getItemMeta();
        if (meta == null) {
            return;
        }

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Уже улучшен — ещё раз нельзя
        if (lore.contains(UPGRADED_LORE_STRING)) {
            event.setResult(null);
            return;
        }

        Material type = resultItem.getType();
        boolean upgraded = false;

        // Немного нормализуем имя, если надо
        String displayName = meta.hasDisplayName()
                ? ChatColor.stripColor(meta.getDisplayName())
                : type.name().toLowerCase();

        // ===== ЛОГИКА УЛУЧШЕНИЯ =====
        // 1) Броня (включая медную)
        if (isArmor(type)) {
            upgraded = upgradeArmor(meta, type);
        }
        // 2) Оружие / инструменты (включая медную кирку и т.д.)
        else if (isToolOrWeapon(type)) {
            upgraded = upgradeToolOrWeapon(meta, type);
        }

        // Ничего не подошло — выходим
        if (!upgraded) {
            return;
        }

        // Добавляем лор-маркер
        lore.add(UPGRADED_LORE_STRING);
        meta.setLore(lore);
        resultItem.setItemMeta(meta);

        // Результат в наковальне
        event.setResult(resultItem);

        // Да, метод устаревший, но это только варнинг
        inventory.setRepairCost(20);
    }

    // ======= ХЕЛПЕРЫ =======

    private boolean isArmor(Material type) {
        String name = type.name();
        // Любая ванильная броня, включая медь: *_HELMET, *_CHESTPLATE, *_LEGGINGS, *_BOOTS
        return name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS");
    }

    private boolean isToolOrWeapon(Material type) {
        String name = type.name();
        // Мечи, топоры, кирки, лопаты, мотыги — всех пород, включая COPPER_*
        return name.endsWith("_SWORD")
                || name.endsWith("_AXE")
                || name.endsWith("_PICKAXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE");
    }

    /**
     * Улучшение брони:
     * - Protection IV
     * - Unbreaking III
     * - Для незеритовой ещё Thorns III
     * Работает для любой брони (включая медную).
     */
    private boolean upgradeArmor(ItemMeta meta, Material type) {
        String name = type.name();

        int prot = 4;
        int unbreaking = 3;
        int thorns = 0;

        if (name.startsWith("LEATHER_")) {
            prot = 3;
            unbreaking = 2;
        } else if (name.startsWith("CHAINMAIL_")) {
            prot = 3;
            unbreaking = 3;
        } else if (name.startsWith("IRON_")) {
            prot = 4;
            unbreaking = 3;
        } else if (name.startsWith("GOLDEN_")) {
            prot = 4;
            unbreaking = 4;
        } else if (name.startsWith("DIAMOND_")) {
            prot = 5;
            unbreaking = 4;
        } else if (name.startsWith("NETHERITE_")) {
            prot = 6;
            unbreaking = 5;
            thorns = 3;
        } else if (name.startsWith("COPPER_")) {
            // медь что-то среднее
            prot = 4;
            unbreaking = 3;
        }

        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, prot, true);
        meta.addEnchant(Enchantment.DURABILITY, unbreaking, true);

        if (thorns > 0) {
            meta.addEnchant(Enchantment.THORNS, thorns, true);
        }

        return true;
    }

    /**
     * Улучшение оружия/инструментов:
     * - мечи/топоры: Sharpness + Unbreaking
     * - кирки/лопаты/мотыги: Efficiency + Unbreaking, для старших тиров — мощнее
     * Это и есть то, чем ты потом ломаешь обсидиан/бедрок (лоре + логика в BlockBreakListener).
     */
    private boolean upgradeToolOrWeapon(ItemMeta meta, Material type) {
        String name = type.name();

        // МЕЧИ
        if (name.endsWith("_SWORD")) {
            int sharp = 5;
            int unbreaking = 3;

            if (name.startsWith("IRON_")) {
                sharp = 6;
            } else if (name.startsWith("DIAMOND_")) {
                sharp = 7;
                unbreaking = 4;
            } else if (name.startsWith("NETHERITE_")) {
                sharp = 8;
                unbreaking = 5;
            } else if (name.startsWith("COPPER_")) {
                sharp = 6;
            }

            meta.addEnchant(Enchantment.DAMAGE_ALL, sharp, true);
            meta.addEnchant(Enchantment.DURABILITY, unbreaking, true);
            return true;
        }

        // ТОПОРЫ
        if (name.endsWith("_AXE")) {
            int sharp = 5;
            int unbreaking = 3;

            if (name.startsWith("IRON_")) {
                sharp = 6;
            } else if (name.startsWith("DIAMOND_")) {
                sharp = 7;
                unbreaking = 4;
            } else if (name.startsWith("NETHERITE_")) {
                sharp = 8;
                unbreaking = 5;
            } else if (name.startsWith("COPPER_")) {
                sharp = 6;
            }

            meta.addEnchant(Enchantment.DAMAGE_ALL, sharp, true);
            meta.addEnchant(Enchantment.DURABILITY, unbreaking, true);
            return true;
        }

        // КИРКИ / ЛОПАТЫ / МОТЫГИ
        if (name.endsWith("_PICKAXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE")) {

            int eff = 5;
            int unbreaking = 3;

            if (name.startsWith("STONE_") || name.startsWith("WOODEN_")) {
                eff = 6;
            } else if (name.startsWith("IRON_")) {
                eff = 10;
            } else if (name.startsWith("GOLDEN_")) {
                eff = 12;
            } else if (name.startsWith("DIAMOND_")) {
                eff = 18;
                unbreaking = 4;
            } else if (name.startsWith("NETHERITE_")) {
                eff = 25;
                unbreaking = 5;
            } else if (name.startsWith("COPPER_")) {
                eff = 9;
            }

            meta.addEnchant(Enchantment.DIG_SPEED, eff, true);
            meta.addEnchant(Enchantment.DURABILITY, unbreaking, true);
            return true;
        }

        return false;
    }
}
