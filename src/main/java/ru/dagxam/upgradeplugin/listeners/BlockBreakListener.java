package ru.dagxam.upgradeplugin.listeners;

// Мы больше не используем 'BlockDamageAbortEvent' или 'PlayerStopMiningEvent'

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
    // Карта для отслеживания "последнего удара"
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
        
        // --- НОВАЯ ЛОГИКА ПРОВЕРКИ ВРЕМЕНИ ---
        long currentTick = player.getWorld().getFullTime();
        long lastTick = lastMineTick.getOrDefault(player.getUniqueId(), 0L);
        
        BlockBreakProgress progress = miningProgress.computeIfAbsent(player.getUniqueId(), k -> new BlockBreakProgress(block));

        // Если игрок сменил блок ИЛИ пропустил больше 1 тика (перестал копать)
        if (!progress.getBlock().equals(block) || (currentTick > lastTick + 1)) {
            // Сбрасываем прогресс
            progress = new BlockBreakProgress(block);
            miningProgress.put(player.getUniqueId(), progress);
        }
        // --- КОНЕЦ НОВОЙ ЛОГИКИ ---

        // 1. Логика для Бедрока (Незеритовая кирка)
        if (blockType == Material.BEDROCK && toolType == Material.NETHERITE_PICKAXE) {
            progress.increment();
            if (progress.getTicks() >= BEDROCK_BREAK_TIME) {
                event.setInstaBreak(true); 
                miningProgress.remove(player.getUniqueId());
            }
        }
        // 2. Логика для Обсидиана (Железная, Золотая или Медная кирка)
        else if (blockType == Material.OBSIDIAN && (
                 toolType == Material.IRON_PICKAXE || 
                 toolType == Material.GOLDEN_PICKAXE ||
                 ItemManager.isCopperTool(tool))) 
        {
            progress.increment();
            if (progress.getTicks() >= OBSIDIAN_BREAK_TIME) {
                event.setInstaBreak(true); 
                miningProgress.remove(player.getUniqueId());
            }
        }
        else {
            // Если это не тот блок, сбрасываем прогресс на всякий случай
            miningProgress.remove(player.getUniqueId());
        }
        
        // Запоминаем тик этого удара
        lastMineTick.put(player.getUniqueId(), currentTick);
    }

    
    // МЫ БОЛЬШЕ НЕ НУЖДАЕМСЯ В 'BlockDamageAbortEvent' ИЛИ 'PlayerStopMiningEvent'


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Сбрасываем кэш тиков на всякий случай
        lastMineTick.remove(player.getUniqueId());
        miningProgress.remove(player.getUniqueId());

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
