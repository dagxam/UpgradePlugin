package ru.dagxam.upgradeplugin.listeners;

import com.destroystokyo.paper.event.block.BlockDamageAbortEvent; // <-- НУЖЕН ИМПОРТ (PAPER API)
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

    // Времена добычи (в тиках, 20 тиков = 1 секунда)
    // Ванильная алмазная кирка ломает обсидиан за 9.4с (188 тиков)
    // Ванильная незеритовая кирка ломает обсидиан за 8.35с (167 тиков)
    private static final int OBSIDIAN_BREAK_TIME = 188; // Как алмазной киркой
    private static final int BEDROCK_BREAK_TIME = 167; // Как незеритовой киркой обсидиан

    // Хранилище прогресса добычи
    private final Map<UUID, BlockBreakProgress> miningProgress = new HashMap<>();

    /**
     * Внутренний класс для отслеживания, что и как долго копает игрок
     */
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


    /**
     * Вызывается КАЖДЫЙ ТИК, когда игрок бьет блок
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return; // Работает только в выживании

        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!ItemManager.isUpgraded(tool)) {
            miningProgress.remove(player.getUniqueId()); // Сбрасываем прогресс, если инструмент не тот
            return;
        }

        Material toolType = tool.getType();
        Material blockType = block.getType();
        
        BlockBreakProgress progress = miningProgress.computeIfAbsent(player.getUniqueId(), k -> new BlockBreakProgress(block));

        // Если игрок сменил блок, сбрасываем прогресс
        if (!progress.getBlock().equals(block)) {
            progress = new BlockBreakProgress(block);
            miningProgress.put(player.getUniqueId(), progress);
        }

        // 1. Логика для Бедрока (Незеритовая кирка)
        if (blockType == Material.BEDROCK && toolType == Material.NETHERITE_PICKAXE) {
            progress.increment();
            if (progress.getTicks() >= BEDROCK_BREAK_TIME) {
                event.setInstaBreak(true); // Ломаем блок
                miningProgress.remove(player.getUniqueId());
            }
        }
        // 2. Логика для Обсидиана (Железная, Золотая или Медная кирка)
        else if (blockType == Material.OBSIDIAN && (
                 toolType == Material.IRON_PICKAXE || 
                 toolType == Material.GOLDEN_PICKAXE ||
                 ItemManager.isCopperTool(tool))) // Проверяем, медная ли это кирка
        {
            progress.increment();
            if (progress.getTicks() >= OBSIDIAN_BREAK_TIME) {
                event.setInstaBreak(true); // Ломаем блок
                miningProgress.remove(player.getUniqueId());
            }
        }
        // Если это другой блок, сбрасываем прогресс
        else {
            miningProgress.remove(player.getUniqueId());
        }
    }

    /**
     * Вызывается, когда игрок ПЕРЕСТАЕТ бить блок (эксклюзивно для Paper)
     */
    @EventHandler
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        // Сбрасываем прогресс добычи, если игрок отпустил кнопку
        miningProgress.remove(event.getPlayer().getUniqueId());
    }


    /**
     * Вызывается, когда блок УЖЕ сломан (чтобы настроить дроп)
     */
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
