package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import ru.dagxam.upgradeplugin.items.ItemManager;

public class BlockBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Ломаем только улучшенным инструментом
        if (!ItemManager.isUpgraded(tool)) {
            return;
        }

        Material toolType = tool.getType();
        Material blockType = block.getType();

        // 1. Бедрок — только улучшенная НАЗЕРИТОВАЯ кирка
        if (blockType == Material.BEDROCK && toolType == Material.NETHERITE_PICKAXE) {
            event.setInstaBreak(true);
            return;
        }

        // 2. Обсидиан — улучшенные железная / золотая / алмазная / незеритовая / медная кирки
        if (blockType == Material.OBSIDIAN &&
                (toolType == Material.IRON_PICKAXE
                        || toolType == Material.GOLDEN_PICKAXE
                        || toolType == Material.DIAMOND_PICKAXE
                        || toolType == Material.NETHERITE_PICKAXE
                        || ItemManager.isCopperTool(tool))) {
            event.setInstaBreak(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!ItemManager.isUpgraded(tool)) {
            return;
        }

        Block block = event.getBlock();
        Material toolType = tool.getType();
        Material blockType = block.getType();

        // 1. Дроп обсидиана
        if (blockType == Material.OBSIDIAN &&
                (toolType == Material.IRON_PICKAXE
                        || toolType == Material.GOLDEN_PICKAXE
                        || toolType == Material.DIAMOND_PICKAXE
                        || toolType == Material.NETHERITE_PICKAXE
                        || ItemManager.isCopperTool(tool))) {

            event.setDropItems(false);
            block.getWorld().dropItemNaturally(
                    block.getLocation().add(0.5, 0.5, 0.5),
                    new ItemStack(Material.OBSIDIAN)
            );
            return;
        }

        // 2. Дроп бедрока — только улучшенная незеритовая кирка
        if (blockType == Material.BEDROCK && toolType == Material.NETHERITE_PICKAXE) {
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(
                    block.getLocation().add(0.5, 0.5, 0.5),
                    new ItemStack(Material.BEDROCK)
            );
        }
    }
}
