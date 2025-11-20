package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    // тот же самый маркер, что и в AnvilListener
    private static final String UPGRADED_LORE_STRING = "§b[Улучшено]";

    // прогресс добычи по игроку
    private static class MiningProgress {
        final Block block;
        int hits;

        MiningProgress(Block block) {
            this.block = block;
            this.hits = 0;
        }
    }

    // player UUID -> его текущий прогресс добычи
    private final Map<UUID, MiningProgress> progressMap = new HashMap<>();

    // ====== ОБРАБОТКА УСКОРЕННОЙ ДОБЫЧИ (ВАРИАНТ B) ======

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Только выживание/приключение
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            resetProgress(player);
            return;
        }

        // Только кирки
        if (!isPickaxe(item)) {
            resetProgress(player);
            return;
        }

        // Только улучшенные кирки
        if (!isUpgraded(item)) {
            resetProgress(player);
            return;
        }

        Material blockType = block.getType();
        Material pickType = item.getType();

        // Определяем нужное количество "ударов" (срабатываний BlockDamageEvent)
        int requiredHits = getRequiredHits(pickType, blockType);

        // Если этот блок для этой кирки не должен ломаться особым образом
        if (requiredHits <= 0) {
            resetProgress(player);
            return;
        }

        UUID uuid = player.getUniqueId();
        MiningProgress mp = progressMap.get(uuid);

        // если игрок начал новый блок — сбрасываем
        if (mp == null || mp.block == null || !mp.block.getLocation().equals(block.getLocation())) {
            mp = new MiningProgress(block);
            progressMap.put(uuid, mp);
        }

        mp.hits++;

        // BEDROCK — ломаем вручную, setInstaBreak не сработает
        if (blockType == Material.BEDROCK) {
            if (mp.hits >= requiredHits) {
                breakBedrock(block, player);
                resetProgress(player);
                event.setCancelled(true);
            } else {
                // не даём моментально сломать, пусть продолжает "ковырять"
                event.setCancelled(true);
            }
            return;
        }

        // Остальные блоки — включая обсидиан и любые другие
        if (mp.hits >= requiredHits) {
            // Разрешаем моментальный слом блока в этот тик
            event.setInstaBreak(true);
            resetProgress(player);
        } else {
            // Продолжаем процесс, но не даём сломать раньше времени
            event.setInstaBreak(false);
        }
    }

    // Чисто на всякий случай: если блок таки сломали,
    // а мы где-то не обнулили прогресс — почистим.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        resetProgress(player);

        // Доп. логика дропа обсидиана/бедрока у тебя, вероятно, уже была.
        // BEDROCK мы уже ломаем вручную в onBlockDamage,
        // поэтому сюда с BEDROCK мы почти не попадём.
        // OBSIDIAN — ломается нормально с дропом по ванильным правилам.
    }

    // ====== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ======

    private void resetProgress(Player player) {
        progressMap.remove(player.getUniqueId());
    }

    private boolean isPickaxe(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.WOODEN_PICKAXE
                || type == Material.STONE_PICKAXE
                || type == Material.IRON_PICKAXE
                || type == Material.GOLDEN_PICKAXE
                || type == Material.DIAMOND_PICKAXE
                || type == Material.NETHERITE_PICKAXE
                // на случай, если в API уже есть медная кирка:
                || type.name().equals("COPPER_PICKAXE");
    }

    private boolean isUpgraded(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        List<String> lore = meta.getLore();
        if (lore == null) return false;
        return lore.contains(UPGRADED_LORE_STRING);
    }

    /**
     * Возвращает требуемое количество "ударов" (срабатываний BlockDamageEvent)
     * для заданной комбинации кирка+блок.
     *
     * 0 или отрицательное — значит, не используем кастомную механику.
     */
    private int getRequiredHits(Material pick, Material block) {
        String pickName = pick.name();

        // BEDROCK — только улучшенная НЕЗЕРИТОВАЯ кирка
        if (block == Material.BEDROCK) {
            if (pick == Material.NETHERITE_PICKAXE) {
                return 6; // твой параметр
            }
            // остальные кирки бедрок не трогают
            return 0;
        }

        // OBSIDIAN — твои значения
        if (block == Material.OBSIDIAN) {
            if (pick == Material.WOODEN_PICKAXE)      return 12;
            if (pick == Material.STONE_PICKAXE)       return 10;
            if (pickName.equals("COPPER_PICKAXE"))    return 9;
            if (pick == Material.IRON_PICKAXE)        return 8;
            if (pick == Material.GOLDEN_PICKAXE)      return 7;
            if (pick == Material.DIAMOND_PICKAXE)     return 5;
            if (pick == Material.NETHERITE_PICKAXE)   return 3;
            return 0;
        }

        // Остальные блоки — ускоренная добыча:
        // WOODEN: 6
        // STONE:  5
        // COPPER: 4
        // IRON:   3
        // GOLDEN: 3
        // DIAMOND:2
        // NETHERITE:2

        if (pick == Material.WOODEN_PICKAXE)          return 6;
        if (pick == Material.STONE_PICKAXE)           return 5;
        if (pickName.equals("COPPER_PICKAXE"))        return 4;
        if (pick == Material.IRON_PICKAXE)            return 3;
        if (pick == Material.GOLDEN_PICKAXE)          return 3;
        if (pick == Material.DIAMOND_PICKAXE)         return 2;
        if (pick == Material.NETHERITE_PICKAXE)       return 2;

        return 0;
    }

    private void breakBedrock(Block block, Player player) {
        Location loc = block.getLocation();
        block.setType(Material.AIR);
        // дропаем бедрок как предмет
        ItemStack drop = new ItemStack(Material.BEDROCK, 1);
        block.getWorld().dropItemNaturally(loc, drop);
        block.getWorld().playSound(loc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
    }
}
