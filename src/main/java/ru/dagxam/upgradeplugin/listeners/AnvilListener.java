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

    private static final String UPGRADED_LORE_STRING = "§b[Улучшено]";

    public AnvilListener(UpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack baseItem = inv.getItem(0);
        ItemStack book = inv.getItem(1);

        // Нужен предмет + книга улучшения
        if (baseItem == null || book == null || !ItemManager.isUpgradeBook(book)) {
            return;
        }

        ItemStack result = baseItem.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Уже улучшен
        if (lore.contains(UPGRADED_LORE_STRING)) {
            event.setResult(null);
            return;
        }

        Material type = result.getType();
        boolean upgraded = false;

        if (isArmor(type)) {
            upgraded = upgradeArmor(meta, type);
        } else if (isToolOrWeapon(type)) {
            upgraded = upgradeToolOrWeapon(meta, type);
        }

        if (!upgraded) return;

        lore.add(UPGRADED_LORE_STRING);
        meta.setLore(lore);
        result.setItemMeta(meta);

        event.setResult(result);
        inv.setRepairCost(20); // только warning, но не ошибка
    }

    // ====== Классификация ======

    private boolean isArmor(Material type) {
        String n = type.name();
        return n.endsWith("_HELMET")
                || n.endsWith("_CHESTPLATE")
                || n.endsWith("_LEGGINGS")
                || n.endsWith("_BOOTS");
    }

    private boolean isToolOrWeapon(Material type) {
        String n = type.name();
        return n.endsWith("_SWORD")
                || n.endsWith("_AXE")
                || n.endsWith("_PICKAXE")
                || n.endsWith("_SHOVEL")
                || n.endsWith("_HOE");
    }

    // ====== Броня ======

    private boolean upgradeArmor(ItemMeta meta, Material type) {
        String name = type.name();

        int prot = 4;
        int unbreak = 3;
        int thorns = 0;

        if (name.startsWith("LEATHER_")) {
            prot = 3; unbreak = 2;
        } else if (name.startsWith("CHAINMAIL_")) {
            prot = 3;
        } else if (name.startsWith("IRON_") || name.startsWith("COPPER_")) {
            prot = 4;
        } else if (name.startsWith("GOLDEN_")) {
            prot = 4; unbreak = 4;
        } else if (name.startsWith("DIAMOND_")) {
            prot = 5; unbreak = 4;
        } else if (name.startsWith("NETHERITE_")) {
            prot = 6; unbreak = 5; thorns = 3;
        }

        meta.addEnchant(Enchantment.PROTECTION, prot, true);
        meta.addEnchant(Enchantment.UNBREAKING, unbreak, true);
        if (thorns > 0) meta.addEnchant(Enchantment.THORNS, thorns, true);

        return true;
    }

    // ====== Инструменты / оружие ======

    private boolean upgradeToolOrWeapon(ItemMeta meta, Material type) {
        String name = type.name();

        // кирки / лопаты / мотыги — Efficiency + Unbreaking
        if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        }

        // мечи и топоры — Sharpness + Unbreaking
        if (name.endsWith("_SWORD") || name.endsWith("_AXE")) {
            meta.addEnchant(Enchantment.SHARPNESS, 6, true);
            meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        }

        applyMaterialCombatBonus(meta, type);

        return true;
    }

    // ====== Бонус к урону и скорости (база + бонус) ======

    private void applyMaterialCombatBonus(ItemMeta meta, Material type) {
        double bonus = getBonusForMaterial(type);
        if (bonus == 0.0) return;

        double baseDamage = getBaseDamage(type);
        double baseSpeed  = getBaseSpeed(type);

        // цель: базовое значение + бонус
        double targetDamage = baseDamage + bonus;
        double targetSpeed  = baseSpeed + bonus;

        // атрибуты по умолчанию: damage = 1 + модификаторы, speed = 4 + модификаторы
        double damageModifierValue = targetDamage - 1.0;
        double speedModifierValue  = targetSpeed - 4.0;

        // убираем старые модификаторы, ставим свои
        meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.ATTACK_SPEED);

        AttributeModifier dmgMod = new AttributeModifier(
                UUID.randomUUID(),
                "upgrade_dmg",
                damageModifierValue,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
        );

        AttributeModifier spdMod = new AttributeModifier(
                UUID.randomUUID(),
                "upgrade_speed",
                speedModifierValue,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
        );

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, dmgMod);
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, spdMod);
    }

    // ====== Таблица бонусов по материалам ======

    private double getBonusForMaterial(Material type) {
        String n = type.name();

        if (n.startsWith("WOODEN_"))    return 2;
        if (n.startsWith("STONE_"))     return 4;
        if (n.startsWith("COPPER_"))    return 5;
        if (n.startsWith("IRON_"))      return 8;
        if (n.startsWith("GOLDEN_"))    return 9;
        if (n.startsWith("DIAMOND_"))   return 12;
        if (n.startsWith("NETHERITE_")) return 15;

        return 0;
    }

    // ====== Ванильный урон (то, что ты видишь до улучшения) ======

    private double getBaseDamage(Material type) {
        String n = type.name();

        // МЕЧИ
        if (n.endsWith("_SWORD")) {
            if (n.startsWith("WOODEN_") || n.startsWith("GOLDEN_")) return 4.0;
            if (n.startsWith("STONE_"))    return 5.0;
            if (n.startsWith("COPPER_"))   return 5.0; // между камнем и железом
            if (n.startsWith("IRON_"))     return 6.0;
            if (n.startsWith("DIAMOND_"))  return 7.0;
            if (n.startsWith("NETHERITE_"))return 8.0;
        }

        // ТОПОРЫ
        if (n.endsWith("_AXE")) {
            if (n.startsWith("WOODEN_"))   return 7.0;
            if (n.startsWith("STONE_"))    return 9.0;
            if (n.startsWith("COPPER_"))   return 8.0;
            if (n.startsWith("IRON_"))     return 9.0;
            if (n.startsWith("GOLDEN_"))   return 7.0;
            if (n.startsWith("DIAMOND_"))  return 9.0;
            if (n.startsWith("NETHERITE_"))return 10.0;
        }

        // КИРКИ
        if (n.endsWith("_PICKAXE")) {
            if (n.startsWith("WOODEN_"))   return 2.0;
            if (n.startsWith("STONE_"))    return 3.0;
            if (n.startsWith("COPPER_"))   return 3.5;
            if (n.startsWith("IRON_"))     return 4.0;
            if (n.startsWith("GOLDEN_"))   return 2.0;
            if (n.startsWith("DIAMOND_"))  return 5.0;
            if (n.startsWith("NETHERITE_"))return 6.0; // как на скрине
        }

        // ЛОПАТЫ
        if (n.endsWith("_SHOVEL")) {
            if (n.startsWith("WOODEN_"))   return 2.5;
            if (n.startsWith("STONE_"))    return 3.5;
            if (n.startsWith("COPPER_"))   return 4.0;
            if (n.startsWith("IRON_"))     return 4.5;
            if (n.startsWith("GOLDEN_"))   return 2.5;
            if (n.startsWith("DIAMOND_"))  return 5.5;
            if (n.startsWith("NETHERITE_"))return 6.5;
        }

        // МОТЫГИ
        if (n.endsWith("_HOE")) {
            if (n.startsWith("WOODEN_"))   return 1.0;
            if (n.startsWith("STONE_"))    return 1.0;
            if (n.startsWith("COPPER_"))   return 1.0;
            if (n.startsWith("IRON_"))     return 1.0;
            if (n.startsWith("GOLDEN_"))   return 1.0;
            if (n.startsWith("DIAMOND_"))  return 1.0;
            if (n.startsWith("NETHERITE_"))return 1.0;
        }

        return 1.0; // безопасный дефолт
    }

    // ====== Ванильная скорость атаки ======

    private double getBaseSpeed(Material type) {
        String n = type.name();

        // МЕЧИ — 1.6
        if (n.endsWith("_SWORD")) {
            return 1.6;
        }

        // ТОПОРЫ — около 0.8–1.0
        if (n.endsWith("_AXE")) {
            if (n.startsWith("WOODEN_") || n.startsWith("STONE_")) return 0.8;
            if (n.startsWith("COPPER_") || n.startsWith("IRON_"))  return 0.9;
            if (n.startsWith("GOLDEN_") || n.startsWith("DIAMOND_") || n.startsWith("NETHERITE_")) return 1.0;
        }

        // КИРКИ — 1.2
        if (n.endsWith("_PICKAXE")) {
            return 1.2;
        }

        // ЛОПАТЫ — 1.0
        if (n.endsWith("_SHOVEL")) {
            return 1.0;
        }

        // МОТЫГИ — разные
        if (n.endsWith("_HOE")) {
            if (n.startsWith("WOODEN_"))   return 1.0;
            if (n.startsWith("STONE_"))    return 2.0;
            if (n.startsWith("COPPER_"))   return 2.5;
            if (n.startsWith("IRON_"))     return 3.0;
            if (n.startsWith("GOLDEN_"))   return 1.0;
            if (n.startsWith("DIAMOND_"))  return 4.0;
            if (n.startsWith("NETHERITE_"))return 4.0;
        }

        return 4.0; // дефолт игрока, если что-то экзотическое
    }
}
