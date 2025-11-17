package ru.dagxam.upgradeplugin.listeners;

// ИСПРАВЛЕННЫЙ ИМПОРТ: io.papermc...
import io.papermc.paper.event.block.BlockDamageAbortEvent; 

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    private static final int OBSIDIAN_BREAK_TIME = 188; // 9.4с (Как алмазной киркой)
    private static final int BEDROCK_BREAK_TIME = 167; // 8.35с (Как незерит обсидиан)

    private final Map<UUID, BlockBreakProgress> miningProgress = new HashMap<>();

    private static class BlockBreakProgress {
        private final Block block;
        private int ticks;

        BlockBreakProgress(Block block) {
            this.block = block;
            this.ticks = 0;
        }

        public Block getBlock() { return block; }
        public int getTicks() { return ticks; }
        public void increment() { this.ticks++; }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return; 

        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // 1. Проверяем, что инструмент в руке УЛУЧШЕН (через ItemManager)
        if (!ItemManager.isUpgraded(tool)) {
            miningProgress.remove(player.getUniqueId()); 
            return;
        }

        Material toolType = tool.getType();
        Material blockType = block.getType();
        
        BlockBreakProgress progress = miningProgress.computeIfAbsent(player.getUniqueId(), k -> new BlockBreakProgress(block));

        if (!progress.getBlock().equals(block)) {
            progress = new BlockBreakProgress(block);
            miningProgress.put(player.getUniqueId(), progress);
        }

        // 2. Логика для Бедрока (Незеритовая кирка)
        if (blockType == Material.BEDROCK && toolType == Material.NETHERITE_PICKAXE) {
            progress.increment();
            if (progress.getTicks() >= BEDROCK_BREAK_TIME) {
                event.setInstaBreak(true); 
                miningProgress.remove(player.getUniqueId());
            }
        }
        // 3. Логика для Обсидиана (Железная, Золотая или Медная кирка)
        else if (blockType == Material.OBSIDIAN && (
                 toolType == Material.IRON_PICKAXE || 
                 toolType == Material.GOLDEN_PICKAXE ||
                 ItemManager.isCopperTool(tool))) // Проверяем, медная ли это кирка
        {
            progress.increment();
            if (progress.getTicks() >= OBSIDIAN_BREAK_TIME) {
                event.setInstaBreak(true); 
                miningProgress.remove(player.getUniqueId());
            }
        }
        else {
            miningProgress.remove(player.getUniqueId());
        }
    }

    /**
     * Вызывается, когда игрок ПЕРЕСТАЕТ бить блок (Paper API)
     */
    @EventHandler
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        // Сбрасываем прогресс добычи, если игрок отпустил кнопку
        miningProgress.remove(event.getPlayer().getUniqueId());
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (!ItemManager.isUpgraded(tool)) return;

        Material toolType = tool.getType();
        Material blockType = block.getType();

        // 1. Дроп Обсидиана (Железо, Золото, Медь)
        if (blockType == Material.OBSIDIAN) {
            if (toolType == Material.IRON_PICKAXE || 
                toolType == Material.GOLDEN_PICKAXE ||
                ItemManager.isCopperTool(tool)) 
            {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), 
                        new ItemStack(Material.OBSIDIAN));
            }
        }

        // 2. Дроп Бедрока (Только Незерит)
        if (blockType == Material.BEDROCK) {
            if (toolType == Material.NETHERITE_PICKAXE) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), 
                        new ItemStack(Material.BEDROCK));
            }
        }
    }
}
