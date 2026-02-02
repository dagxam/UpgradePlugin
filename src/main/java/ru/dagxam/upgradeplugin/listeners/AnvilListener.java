package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import ru.dagxam.upgradeplugin.UpgradePlugin;
import ru.dagxam.upgradeplugin.upgrade.UpgradeManager;

/**
 * Листенер максимально тонкий: только валидация + прокидывание в UpgradeManager.
 */
public class AnvilListener implements Listener {

    private final UpgradePlugin plugin;
    private final UpgradeManager upgradeManager;

    public AnvilListener(UpgradePlugin plugin, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.upgradeManager = upgradeManager;
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack baseItem = inv.getItem(0);
        ItemStack book = inv.getItem(1);

        if (baseItem == null || book == null) return;
        if (!upgradeManager.isUpgradeBook(book)) return;

        ItemStack result = upgradeManager.tryUpgrade(baseItem);
        if (result == null) {
            event.setResult(null);
            return;
        }

        event.setResult(result);
        inv.setRepairCost(plugin.getPluginConfig().getAnvilRepairCost());
    }
}
