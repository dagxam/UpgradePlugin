package ru.dagxam.upgradeplugin.listeners;

// ИСПРАВЛЕНО: Мы УДАЛИЛИ импорт io.papermc.paper.event.player.PlayerStopMiningEvent

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

    private static final int FAST_BREAK_TIME = 20; // 1 секунда

    private final Map<UUID, BlockBreakProgress> miningProgress = new HashMap<>();
    private final Map<UUID, Long> lastMineTick = new HashMap<>();

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

        if (!ItemManager.isUpgraded(tool)) {
            miningProgress.remove(player.getUniqueId()); 
            lastMineTick.remove(player.getUniqueId());
            return;
        }

        Material toolType = tool.getType();
        Material blockType = block.getType();
        
        long currentTick = player.getWorld().getFullTime();
        long lastTick = lastMineTick.getOrDefault(player.getUniqueId(), 0L);
        
        BlockBreakProgress progress = miningProgress.computeIfAbsent(player.getUniqueId(), k -> new BlockBreakProgress(block));

        // Эта проверка сбрасывает прогресс, если игрок сменил блок ИЛИ перестал копать
        if (!progress.getBlock().equals(block) || (currentTick > lastTick + 1)) {
            progress = new BlockBreakProgress(block);
            miningProgress.put(player.getUniqueId(), progress);
        }

        // 1. Логика для Бедрока (Только Незеритовая кирка)
        if (blockType == Material.BEDROCK && toolType == Material.NETHERITE_PICKAXE) {
            progress.increment();
            if (progress.getTicks() >= FAST_BREAK_TIME) { 
                event.setInstaBreak(true); 
                miningProgress.remove(player.getUniqueId());
            }
        }
        // 2. Логика для Обсидиана (ВСЕ улучшенные кирки)
        else if (blockType == Material.OBSIDIAN && (
                 toolType == Material.IRON_PICKAXE || 
                 toolType == Material.GOLDEN_PICKAXE ||
                 toolType == Material.DIAMOND_PICKAXE || 
                 toolType == Material.NETHERITE_PICKAXE || 
                 ItemManager.isCopperTool(tool))) 
        {
            progress.increment();
            if (progress.getTicks() >= FAST_BREAK_TIME) { 
                event.setInstaBreak(true); 
                miningProgress.remove(player.getUniqueId());
            }
        }
        else {
            miningProgress.remove(player.getUniqueId());
        }
        
        lastMineTick.put(player.getUniqueId(), currentTick);
    }

    
    // ИСПРАВЛЕНО: Мы УДАЛИЛИ весь метод onPlayerStopMining(), так как он вызывал ошибку


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        lastMineTick.remove(player.getUniqueId());
        miningProgress.remove(player.getUniqueId());

        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (!ItemManager.isUpgraded(tool)) return;

        Material toolType = tool.getType();
        Material blockType = block.getType();

        // 1. Дроп Обсидиана (ВСЕ улучшенные кирки)
        if (blockType == Material.OBSIDIAN) {
            if (toolType == Material.IRON_PICKAXE || 
                toolType == Material.GOLDEN_PICKAXE ||
                toolType == Material.DIAMOND_PICKAXE || 
                toolType == Material.NETHERITE_PICKAXE || 
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
