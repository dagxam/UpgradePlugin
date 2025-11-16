package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import ru.dagxam.upgradeplugin.items.ItemManager; // Наш ItemManager

public class BlockBreakListener implements Listener {

    /**
     * Это событие срабатывает, когда игрок НАЧИНАЕТ бить блок.
     * Мы используем его, чтобы сделать ломание мгновенным.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // 1. Проверяем, что инструмент в руке УЛУЧШЕН
        if (!ItemManager.isUpgraded(tool)) {
            return;
        }

        Material toolType = tool.getType();
        Material blockType = block.getType();

        // 2. Логика для Бедрока + Улучшенной Незеритовой кирки
        if (blockType == Material.BEDROCK && toolType == Material.NETHERITE_PICKAXE) {
            // Мгновенно ломаем блок (фишка Paper API)
            event.setInstaBreak(true);
            return; // Выходим
        }

        // 3. Логика для Обсидиана + Улучшенной Железной/Золотой кирки
        if (blockType == Material.OBSIDIAN) {
            if (toolType == Material.IRON_PICKAXE || toolType == Material.GOLDEN_PICKAXE) {
                // Мгновенно ломаем (быстрее, чем "как камень", но соответствует силе книги)
                event.setInstaBreak(true);
            }
        }
    }

    /**
     * Это событие срабатывает, когда блок УЖЕ сломан.
     * Мы используем его, чтобы проконтролировать ВЫПАДЕНИЕ предметов (дроп).
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Снова проверяем, что инструмент улучшен
        if (!ItemManager.isUpgraded(tool)) {
            return;
        }

        Material toolType = tool.getType();
        Material blockType = block.getType();

        // 1. Логика для Обсидиана (Железо и Золото)
        if (blockType == Material.OBSIDIAN) {
            if (toolType == Material.IRON_PICKAXE || toolType == Material.GOLDEN_PICKAXE) {
                // Отменяем стандартный дроп (т.е. ничего)
                event.setDropItems(false);
                // Выдаем 1 обсидиан в центре блока
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), 
                        new ItemStack(Material.OBSIDIAN));
            }
        }

        // 2. Логика для Бедрока (Только Незерит)
        if (blockType == Material.BEDROCK) {
            if (toolType == Material.NETHERITE_PICKAXE) {
                // Отменяем стандартный дроп (ничего)
                event.setDropItems(false);
                // Выдаем 1 бедрок
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), 
                        new ItemStack(Material.BEDROCK));
            }
        }
    }
}
