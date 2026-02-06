package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.dagxam.upgradeplugin.upgrade.MaterialTier;
import ru.dagxam.upgradeplugin.upgrade.UpgradeManager;

/**
 * Поддерживает эффекты от УЛУЧШЕННОЙ брони.
 *
 * Логика:
 * - Любая улучшенная DIAMOND броня: Water Breathing
 * - Любая улучшенная NETHERITE броня: Water Breathing + Fire Resistance + Night Vision
 *
 * Эффекты накладываются короткими "пакетами" и постоянно обновляются,
 * поэтому после снятия брони они исчезают сами (мы не снимаем эффекты вручную,
 * чтобы не ломать зелья/маяки и другие источники эффектов).
 */
public final class ArmorEffectsTask implements Runnable {

    private final JavaPlugin plugin;
    private final UpgradeManager upgradeManager;

    // длительность эффекта (в тиках) должна быть больше, чем период обновления
    private static final int EFFECT_DURATION_TICKS = 120; // 6 секунд
    private static final int AMPLIFIER_0 = 0;

    public ArmorEffectsTask(JavaPlugin plugin, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.upgradeManager = upgradeManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyForPlayer(player);
        }
    }

    private void applyForPlayer(Player player) {
        // Определяем "максимальный" уровень по надетым улучшенным частям
        boolean hasUpgradedDiamond = false;
        boolean hasUpgradedNetherite = false;

        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece == null || piece.getType() == Material.AIR) continue;
            if (!upgradeManager.isUpgraded(piece)) continue;

            MaterialTier tier = MaterialTier.fromMaterial(piece.getType());
            if (tier == MaterialTier.NETHERITE) {
                hasUpgradedNetherite = true;
            } else if (tier == MaterialTier.DIAMOND) {
                hasUpgradedDiamond = true;
            }
        }

        if (hasUpgradedNetherite) {
            give(player, PotionEffectType.WATER_BREATHING);
            give(player, PotionEffectType.FIRE_RESISTANCE);
            give(player, PotionEffectType.NIGHT_VISION);
            return;
        }

        if (hasUpgradedDiamond) {
            give(player, PotionEffectType.WATER_BREATHING);
        }
    }

    private void give(Player player, PotionEffectType type) {
        // ambient=true, particles=false, icon=false — чтобы не мешало визуально
        player.addPotionEffect(new PotionEffect(type, EFFECT_DURATION_TICKS, AMPLIFIER_0, true, false, false));
    }
}
