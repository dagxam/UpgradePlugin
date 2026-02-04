package ru.dagxam.upgradeplugin.upgrade;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.dagxam.upgradeplugin.config.PluginConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Вся логика апгрейдов в одном месте.
 * Листенеры становятся тонкими и предсказуемыми.
 */
public class UpgradeManager {

    private final JavaPlugin plugin;
    private final PluginConfig cfg;

    private final NamespacedKey upgradeBookKey;
    private final NamespacedKey upgradedKey;
    private final NamespacedKey upgradedTypeKey;

    public UpgradeManager(JavaPlugin plugin, PluginConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.upgradeBookKey = new NamespacedKey(plugin, "upgrade_book");
        this.upgradedKey = new NamespacedKey(plugin, "upgraded");
        this.upgradedTypeKey = new NamespacedKey(plugin, "upgrade_type");

        cfg.warnIfMisconfigured();
    }

    // ===== Book =====

    public NamespacedKey getUpgradeBookKey() {
        return upgradeBookKey;
    }

    public ItemStack createUpgradeBook() {
        // сохраняем старый тип книги, чтобы она выглядела знакомо
        ItemStack book = new ItemStack(org.bukkit.Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return book;

        meta.setDisplayName(cfg.getBookName());
        meta.setLore(cfg.getBookLore());
        meta.getPersistentDataContainer().set(upgradeBookKey, PersistentDataType.BYTE, (byte) 1);

        book.setItemMeta(meta);
        return book;
    }

    public boolean isUpgradeBook(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(upgradeBookKey, PersistentDataType.BYTE);
    }

    // ===== Upgraded marker =====

    public boolean isUpgraded(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(upgradedKey, PersistentDataType.BYTE);
    }

    public UpgradeType getUpgradeType(ItemStack item) {
        if (item == null) return UpgradeType.NONE;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return UpgradeType.NONE;
        String raw = meta.getPersistentDataContainer().get(upgradedTypeKey, PersistentDataType.STRING);
        if (raw == null) return UpgradeType.NONE;
        try {
            return UpgradeType.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return UpgradeType.NONE;
        }
    }

    private void markUpgraded(ItemMeta meta, UpgradeType type) {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(upgradedKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(upgradedTypeKey, PersistentDataType.STRING, type.name());

        // оставляем видимый маркер в лоре для игрока
        String marker = cfg.getUpgradedLoreMarker();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        if (!lore.contains(marker)) {
            lore.add(marker);
            meta.setLore(lore);
        }
    }

    // ===== Apply upgrade =====

    /**
     * Применяет апгрейд к предмету и возвращает НОВЫЙ ItemStack.
     * Если апгрейд невозможен (не тот тип/уже улучшен) — вернет null.
     */
    public ItemStack tryUpgrade(ItemStack baseItem) {
        if (baseItem == null) return null;
        if (isUpgraded(baseItem)) return null;

        UpgradeType type = UpgradeType.fromMaterial(baseItem.getType());
        if (type == UpgradeType.NONE) return null;

        ItemStack result = baseItem.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return null;

        boolean applied;
        switch (type) {
            case ARMOR -> applied = applyArmor(meta, MaterialTier.fromMaterial(result.getType()));
            case TOOL -> applied = applyTool(meta);
            case WEAPON -> applied = applyWeapon(meta, result.getType());
            default -> applied = false;
        }
        if (!applied) return null;

        markUpgraded(meta, type);
        result.setItemMeta(meta);
        return result;
    }

    private boolean applyArmor(ItemMeta meta, MaterialTier tier) {
        // дефолты, если конфига нет
        int prot = switch (tier) {
            case LEATHER -> 3;
            case CHAINMAIL -> 3;
            case IRON, COPPER -> 4;
            case GOLDEN -> 4;
            case DIAMOND -> 5;
            case NETHERITE -> 6;
            default -> 4;
        };
        int unbreak = switch (tier) {
            case LEATHER -> 2;
            case NETHERITE -> 5;
            case DIAMOND, GOLDEN -> 4;
            default -> 3;
        };
        int thorns = (tier == MaterialTier.NETHERITE) ? 3 : 0;

        prot = cfg.getArmorEnchant(tier, "protection", prot);
        unbreak = cfg.getArmorEnchant(tier, "unbreaking", unbreak);
        thorns = cfg.getArmorEnchant(tier, "thorns", thorns);

        meta.addEnchant(Enchantment.PROTECTION, prot, true);
        meta.addEnchant(Enchantment.UNBREAKING, unbreak, true);
        if (thorns > 0) meta.addEnchant(Enchantment.THORNS, thorns, true);
        return true;
    }

    private boolean applyTool(ItemMeta meta) {
        int eff = cfg.getToolEnchant("efficiency", 10);
        int unbreak = cfg.getToolEnchant("unbreaking", 5);
        meta.addEnchant(Enchantment.EFFICIENCY, eff, true);
        meta.addEnchant(Enchantment.UNBREAKING, unbreak, true);
        return true;
    }

    private boolean applyWeapon(ItemMeta meta, org.bukkit.Material type) {
        int sharp = cfg.getWeaponEnchant("sharpness", 6);
        int unbreak = cfg.getWeaponEnchant("unbreaking", 5);
        meta.addEnchant(Enchantment.SHARPNESS, sharp, true);
        meta.addEnchant(Enchantment.UNBREAKING, unbreak, true);

        // боевые бонусы — через атрибуты
        applyCombatBonus(meta, type);
        return true;
    }

    private AttributeModifier createHandModifier(String name, double amount) {
        // Paper/Bukkit API менялся: где-то используется EquipmentSlot, где-то EquipmentSlotGroup.
        // Делаем совместимость через рефлексию, чтобы проект собирался на разных версиях.
        try {
            Class<?> groupClass = Class.forName("org.bukkit.inventory.EquipmentSlotGroup");
            Object mainHand = groupClass.getField("MAINHAND").get(null);
            return (AttributeModifier) AttributeModifier.class
                    .getConstructor(UUID.class, String.class, double.class, AttributeModifier.Operation.class, groupClass)
                    .newInstance(UUID.randomUUID(), name, amount, AttributeModifier.Operation.ADD_NUMBER, mainHand);
        } catch (Throwable ignored) {
            // старое API
            return new AttributeModifier(
                    UUID.randomUUID(),
                    name,
                    amount,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
            );
        }
    }

    private void applyCombatBonus(ItemMeta meta, org.bukkit.Material type) {
        MaterialTier tier = MaterialTier.fromMaterial(type);
        double bonus = cfg.getCombatBonus(tier);
        if (bonus == 0.0) return;

        // Для скорости/урона используем ванильные базовые значения
        double baseDamage = VanillaStats.baseDamage(type);
        double baseSpeed = VanillaStats.baseSpeed(type);

        double targetDamage = baseDamage + bonus;
        double targetSpeed = baseSpeed + bonus;

        // Ванильные формулы атрибутов: damage = 1 + modifiers, speed = 4 + modifiers
        double dmgModValue = targetDamage - 1.0;
        double spdModValue = targetSpeed - 4.0;

        meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.ATTACK_SPEED);

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, createHandModifier("upgrade_dmg", dmgModValue));
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, createHandModifier("upgrade_speed", spdModValue));
    }
}
