package ru.dagxam.upgradeplugin.listeners;

// НУЖНЫЕ ИМПОРТЫ
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
// -----------------

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
    
    // Сериализатор для получения "чистого" текста из имени предмета
    private final PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();

    public AnvilListener(UpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);

        if (firstItem == null || secondItem == null || !ItemManager.isUpgradeBook(secondItem)) {
            return;
        }

        ItemMeta meta = firstItem.getItemMeta(); 
        if (meta == null) {
            return;
        }
        
        ItemStack resultItem = firstItem.clone();
        ItemMeta resultMeta = resultItem.getItemMeta(); 

        List<String> lore = resultMeta.hasLore() ? new ArrayList<>(resultMeta.getLore()) : new ArrayList<>();
        if (lore.contains("§b[Улучшено]")) {
            event.setResult(null);
            return;
        }

        Material type = resultItem.getType();
        boolean success = false;
        
        // --- Самый надежный способ получить имя предмета (Paper API) ---
        Component nameComponent = firstItem.displayName();
        String displayName = plainTextSerializer.serialize(nameComponent).trim();
        // ----------------------------------------------------------------

        // --- ЛОГИКА УЛУЧШЕНИЯ ---

        // (Медная броня)
        String lowerName = displayName.toLowerCase(); // Для надежной проверки
        
        if (lowerName.startsWith("медная кираса") || lowerName.startsWith("copper chestplate") ||
            lowerName.startsWith("медный шлем") || lowerName.startsWith("copper helmet") ||
            lowerName.startsWith("медные поножи") || lowerName.startsWith("copper leggings") ||
            lowerName.startsWith("медные ботинки") || lowerName.startsWith("copper boots")) 
        {
            applyArmorBonus(resultMeta, type, getSlot(type), 3.0, 6.0, 0, 0, displayName); 
            applyDurability(resultMeta, 3);
            success = true;
        }
        // (Медные инструменты)
        else if (lowerName.startsWith("медная кирка") || lowerName.startsWith("copper pickaxe") ||
                 lowerName.startsWith("медный топор") || lowerName.startsWith("copper axe") ||
                 lowerName.startsWith("медная лопата") || lowerName.startsWith("copper shovel") ||
                 lowerName.startsWith("медная мотыга") || lowerName.startsWith("copper hoe") ||
                 lowerName.startsWith("медный меч") || lowerName.startsWith("copper sword")) 
        {
            applyWeaponBonus(resultMeta, type, 6.0, 6.0, displayName); 
            if (lowerName.startsWith("медная кирка") || lowerName.startsWith("copper pickaxe")) {
                resultMeta.addEnchant(Enchantment.EFFICIENCY, 12, true);
            }
            success = true;
        }
        // Кожа
        else if (type.name().startsWith("LEATHER_")) {
            applyArmorBonus(resultMeta, type, getSlot(type), 2.0, 4.0, 0, 0, displayName); 
            applyDurability(resultMeta, 2);
            success = true;
        }
        // Кольчуга
        else if (type.name().startsWith("CHAINMAIL_")) {
            applyArmorBonus(resultMeta, type, getSlot(type), 4.0, 8.0, 0, 0, displayName); 
            applyDurability(resultMeta, 4);
            success = true;
        }
        // Железо
        else if (type.name().startsWith("IRON_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(resultMeta, type, getSlot(type), 6.0, 12.0, 0, 0, displayName); 
                applyDurability(resultMeta, 6);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(resultMeta, type, 8.0, 8.0, displayName);
                if (type == Material.IRON_PICKAXE) {
                    resultMeta.addEnchant(Enchantment.EFFICIENCY, 15, true); 
                }
                success = true;
            }
        }
        // Золото
        else if (type.name().startsWith("GOLDEN_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(resultMeta, type, getSlot(type), 7.0, 14.0, 0, 0, displayName);
                applyDurability(resultMeta, 7);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(resultMeta, type, 9.0, 9.0, displayName);
                if (type == Material.GOLDEN_PICKAXE) {
                    resultMeta.addEnchant(Enchantment.EFFICIENCY, 12, true);
                }
                success = true;
            }
        }
        // Алмазы
        else if (type.name().startsWith("DIAMOND_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(resultMeta, type, getSlot(type), 10.0, 20.0, 10.0, 0, displayName); 
                applyDurability(resultMeta, 10);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(resultMeta, type, 12.0, 12.0, displayName);
                if (type == Material.DIAMOND_PICKAXE) {
                    resultMeta.addEnchant(Enchantment.EFFICIENCY, 20, true);
                }
                success = true;
            }
        }
        // Незерит
        else if (type.name().startsWith("NETHERITE_")) {
            if (getSlot(type) != null) { // Броня
                applyArmorBonus(resultMeta, type, getSlot(type), 15.0, 30.0, 15.0, 1.5, displayName); 
                applyDurability(resultMeta, 15);
                success = true;
            } else { // Инструменты/Оружие
                applyWeaponBonus(resultMeta, type, 15.0, 15.0, displayName);
                if (type == Material.NETHERITE_PICKAXE) {
                    resultMeta.addEnchant(Enchantment.EFFICIENCY, 25, true);
                }
                success = true;
            }
        }
        // Дерево
        else if (type.name().startsWith("WOODEN_")) {
            applyWeaponBonus(resultMeta, type, 2.0, 2.0, displayName);
            if (type == Material.WOODEN_PICKAXE) {
                resultMeta.addEnchant(Enchantment.EFFICIENCY, 8, true);
            }
            success = true;
        }
        // Камень
        else if (type.name().startsWith("STONE_")) {
            applyWeaponBonus(resultMeta, type, 4.0, 4.0, displayName);
            if (type == Material.STONE_PICKAXE) {
                resultMeta.addEnchant(Enchantment.EFFICIENCY, 10, true);
            }
            success = true;
        }

        // --- КОНЕЦ ЛОГИКИ ---

        if (success) {
            lore.add("§b[Улучшено]");
            resultMeta.setLore(lore);
            resultItem.setItemMeta(resultMeta); 
            event.setResult(resultItem);
            inventory.setRepairCost(20);
        }
    }

    /**
     * Применяет бонусы к ОРУЖИЮ/ИНСТРУМЕНТАМ, суммируя их с ванильными.
     */
    private void applyWeaponBonus(ItemMeta meta, Material type, double damageBonus, double speedBonus, String displayName) {
        
        double baseDamage = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_DAMAGE, displayName);
        double newDamage = baseDamage + damageBonus;
        
        double baseSpeed = getVanillaAttribute(type, Attribute.GENERIC_ATTACK_SPEED, displayName);
        double newSpeed = baseSpeed + speedBonus;
        
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, 
            new AttributeModifier(UUID.randomUUID(), "UpgradeDamage", newDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
