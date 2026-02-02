package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import ru.dagxam.upgradeplugin.UpgradePlugin;
import ru.dagxam.upgradeplugin.config.PluginConfig;
import ru.dagxam.upgradeplugin.upgrade.UpgradeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Добавляет книгу улучшения в трейды библиотекаря.
 * Важно: VillagerAcquireTradeEvent может срабатывать много раз — защищаемся от дублирования.
 */
public class VillagerListener implements Listener {

    private final UpgradePlugin plugin;
    private final PluginConfig cfg;
    private final UpgradeManager upgradeManager;
    private final Random random = new Random();
    private final NamespacedKey tradeAddedKey;

    public VillagerListener(UpgradePlugin plugin, PluginConfig cfg, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.upgradeManager = upgradeManager;
        this.tradeAddedKey = new NamespacedKey(plugin, "upgrade_trade_added");
    }

    @EventHandler
    public void onVillagerTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.getProfession() != Villager.Profession.LIBRARIAN) return;

        // Защита от спама: добавляем только один раз на жизнь жителя
        if (villager.getPersistentDataContainer().has(tradeAddedKey, PersistentDataType.BYTE)) {
            return;
        }

        if (random.nextInt(100) >= cfg.getVillagerChancePercent()) {
            return;
        }

        ItemStack book = upgradeManager.createUpgradeBook();
        MerchantRecipe recipe = new MerchantRecipe(
                book,
                0,
                cfg.getVillagerMaxUses(),
                true,
                cfg.getVillagerXp(),
                cfg.getVillagerPriceMultiplier()
        );
        recipe.addIngredient(new ItemStack(Material.EMERALD, cfg.getVillagerEmeraldCost()));

        List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
        recipes.add(recipe);
        villager.setRecipes(recipes);

        villager.getPersistentDataContainer().set(tradeAddedKey, PersistentDataType.BYTE, (byte) 1);
    }
}
