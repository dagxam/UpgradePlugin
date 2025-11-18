package ru.dagxam.upgradeplugin.listeners;

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

    // Маркер улучшения, который использует BlockBreakListener
    private static final String UPGRADED_LORE_STRING = "§b[Улучшено]";

    public AnvilListener(UpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);   // предмет
        ItemStack secondItem = inventory.getItem(1);  // книга улучшения

        // Должен быть предмет + книга улучшения
        if (firstItem == null || secondItem == null || !ItemManager.isUpgradeBook(secondItem)) {
            return;
        }

        // Даже если меты не было — getItemMeta() вернёт дефолтную
        ItemStack resultItem = firstItem.clone();
        ItemMeta meta = resultItem.getItemMeta();
        if (meta == null) {
            return;
        }

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Уже улучшен — не даём второй раз
        if (lore.contains(UPGRADED_LORE_STRING)) {
            event.setResult(null);
            return;
        }

        Material type = resultItem.getType();
        boolean upgraded = false;

        String displayName = meta.hasDisplayName()
                ? ChatColor.stripColor(meta.getDisplayName())
                : type.name().toLowerCase();

        // ===== ЛОГИКА УЛУЧШЕНИЯ =====

        // 1) Броня (включая медную)
        if (isArmor(type)) {
            upgraded = upgradeArmor(meta, type);
        }
        // 2) Инструменты и оружие (включая медные)
        else if (isToolOrWeapon(type)) {
            upgraded = upgradeToolOrWeapon(meta, type);
        }

        if (!upgraded) {
            return;
        }

        // Добавляем лор-маркер
        lore.add(UPGRADED_LORE_STRING);
        meta.setLore(lore);
        resultItem.setItemMeta(meta);

        // Результат
        event.setResult(resultItem);

        // Deprecated → только warning, можно жить
        inventory.setRepairCost(20);
    }

    // ===== ХЕЛПЕРЫ =====

    private boolean isArmor(Material type) {
        String name = type.name();
        return name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS");
    }

    private boolean isToolOrWeapon(Material type) {
        String name = type.name();
        return name.endsWith("_SWORD")
                || name.endsWith("_AXE")
                || name.endsWith("_PICKAXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE");
    }

    /**
     * Улучшение брони:
     * - PROTECTION X
     * - UNBREAKING X
     * - Для незерита ещё THORNS III
     * Работает для всех типов, включая COPPER_*.
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
            prot = 4;
            unbreaking = 3;
        }

        meta.addEnchant(Enchantment.PROTECTION, prot, true);
        meta.addEnchant(Enchantment.UNBREAKING, unbreaking, true);

        if (thorns > 0) {
            meta.addEnchant(Enchantment.THORNS, thorns, true);
        }

        return true;
    }

    /**
     * Улучшение оружия/инструментов:
     * - мечи/топоры: SHARPNESS + UNBREAKING + буст атрибутов
     * - кирки/лопаты/мотыги: EFFICIENCY + UNBREAKING + буст атрибутов
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

            meta.addEnchant(Enchantment.SHARPNESS, sharp, true);
            meta.addEnchant(Enchantment.UNBREAKING, unbreaking, true);

            applyWeaponAttributes(meta, type);
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

            meta.addEnchant(Enchantment.SHARPNESS, sharp, true);
            meta.addEnchant(Enchantment.UNBREAKING, unbreaking, true);

            applyWeaponAttributes(meta, type);
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

            meta.addEnchant(Enchantment.EFFICIENCY, eff, true);
            meta.addEnchant(Enchantment.UNBREAKING, unbreaking, true);

            applyWeaponAttributes(meta, type);
            return true;
        }

        return false;
    }

    /**
     * Тут мы уже реально двигаем зелёные строки:
     * "Урон при атаке" и "Скорость атаки".
     * Просто добавляем бонус к существующим значениям.
     */
    @SuppressWarnings("deprecation")
    private void applyWeaponAttributes(ItemMeta meta, Material type) {
        String name = type.name();

        double damageBonus = 0.0;
        double speedBonus = 0.0;

        // Простая система: чем сильнее материал, тем больше буст
        if (name.endsWith("_SWORD") || name.endsWith("_AXE")) {
            if (name.startsWith("WOODEN_") || name.startsWith("STONE_")) {
                damageBonus = 2.0;
                speedBonus = 0.2;
            } else if (name.startsWith("COPPER_") || name.startsWith("IRON_")) {
                damageBonus = 3.0;
                speedBonus = 0.3;
            } else if (name.startsWith("GOLDEN_")) {
                damageBonus = 3.0;
                speedBonus = 0.4;
            } else if (name.startsWith("DIAMOND_")) {
                damageBonus = 4.0;
                speedBonus = 0.4;
            } else if (name.startsWith("NETHERITE_")) {
                damageBonus = 5.0;
                speedBonus = 0.5;
            }
        } else if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            if (name.startsWith("WOODEN_") || name.startsWith("STONE_")) {
                damageBonus = 1.0;
                speedBonus = 0.2;
            } else if (name.startsWith("COPPER_") || name.startsWith("IRON_")) {
                damageBonus = 2.0;
                speedBonus = 0.3;
            } else if (name.startsWith("GOLDEN_")) {
                damageBonus = 2.0;
                speedBonus = 0.4;
            } else if (name.startsWith("DIAMOND_")) {
                damageBonus = 3.0;
                speedBonus = 0.4;
            } else if (name.startsWith("NETHERITE_")) {
                damageBonus = 4.0;
                speedBonus = 0.5;
            }
        }

        // Сначала чистим предыдущие кастомные модификаторы
        meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.ATTACK_SPEED);

        // Потом добавляем наш бонус (ADD_NUMBER → просто плюс к базовому)
        if (damageBonus != 0.0) {
            AttributeModifier dmgMod = new AttributeModifier(
                    UUID.randomUUID(),
                    "upgrade_damage",
                    damageBonus,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, dmgMod);
        }

        if (speedBonus != 0.0) {
            AttributeModifier spdMod = new AttributeModifier(
                    UUID.randomUUID(),
                    "upgrade_speed",
                    speedBonus,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.ATTACK_SPEED, spdMod);
        }
    }
}
