package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import ru.dagxam.upgradeplugin.UpgradePlugin;
import ru.dagxam.upgradeplugin.config.PluginConfig;
import ru.dagxam.upgradeplugin.upgrade.MaterialTier;
import ru.dagxam.upgradeplugin.upgrade.UpgradeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ускоренная добыча для улучшенных кирок.
 *
 * Улучшения под нагрузкой:
 * - не храним ссылку на Block (только Location), чтобы не держать чанки лишний раз
 * - чистим progressMap на quit/kick/teleport/worldchange/itemchange
 */
public class BlockBreakListener implements Listener {

    private final UpgradePlugin plugin;
    private final PluginConfig cfg;
    private final UpgradeManager upgradeManager;

    private static final class MiningProgress {
        final World world;
        final int x, y, z;
        final Material blockType;
        int hits;

        MiningProgress(Block block) {
            this.world = block.getWorld();
            Location l = block.getLocation();
            this.x = l.getBlockX();
            this.y = l.getBlockY();
            this.z = l.getBlockZ();
            this.blockType = block.getType();
            this.hits = 0;
        }

        boolean isSameBlock(Block block) {
            if (block == null) return false;
            Location l = block.getLocation();
            return block.getWorld().equals(world)
                    && l.getBlockX() == x
                    && l.getBlockY() == y
                    && l.getBlockZ() == z
                    && block.getType() == blockType;
        }
    }

    private final Map<UUID, MiningProgress> progressMap = new HashMap<>();

    public BlockBreakListener(UpgradePlugin plugin, PluginConfig cfg, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.upgradeManager = upgradeManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!cfg.isMiningEnabled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Только выживание/приключение
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            reset(player);
            return;
        }

        // Только кирки
        if (!isPickaxe(tool)) {
            reset(player);
            return;
        }

        // Только улучшенные
        if (!upgradeManager.isUpgraded(tool)) {
            reset(player);
            return;
        }

        MaterialTier pickTier = MaterialTier.fromMaterial(tool.getType());
        Material blockType = block.getType();

        int requiredHits = defaultRequiredHits(blockType, pickTier);
        requiredHits = cfg.getMiningHits(blockType, pickTier, requiredHits);

        if (requiredHits <= 0) {
            reset(player);
            return;
        }

        UUID uuid = player.getUniqueId();
        MiningProgress mp = progressMap.get(uuid);
        if (mp == null || !mp.isSameBlock(block)) {
            mp = new MiningProgress(block);
            progressMap.put(uuid, mp);
        }

        mp.hits++;

        if (blockType == Material.BEDROCK) {
            // BEDROCK ломаем вручную, setInstaBreak может не сработать
            if (mp.hits >= requiredHits) {
                breakBedrock(block, player);
                reset(player);
            }
            event.setCancelled(true);
            return;
        }

        if (mp.hits >= requiredHits) {
            event.setInstaBreak(true);
            reset(player);
        } else {
            event.setInstaBreak(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        reset(event.getPlayer());
    }

    // ===== Cleanup events (важно под нагрузкой) =====

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        reset(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        reset(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        reset(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        reset(event.getPlayer());
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        reset(event.getPlayer());
    }

    private void reset(Player player) {
        progressMap.remove(player.getUniqueId());
    }

    private boolean isPickaxe(ItemStack item) {
        if (item == null) return false;
        String n = item.getType().name();
        return n.endsWith("_PICKAXE");
    }

    /**
     * Дефолтные значения, если в конфиге нет секции.
     */
    private int defaultRequiredHits(Material block, MaterialTier pickTier) {
        if (block == Material.BEDROCK) {
            return (pickTier == MaterialTier.NETHERITE) ? 6 : 0;
        }
        if (block == Material.OBSIDIAN) {
            return switch (pickTier) {
                case WOODEN -> 12;
                case STONE -> 10;
                case COPPER -> 9;
                case IRON -> 8;
                case GOLDEN -> 7;
                case DIAMOND -> 5;
                case NETHERITE -> 3;
                default -> 0;
            };
        }
        return switch (pickTier) {
            case WOODEN -> 6;
            case STONE -> 5;
            case COPPER -> 4;
            case IRON, GOLDEN -> 3;
            case DIAMOND, NETHERITE -> 2;
            default -> 0;
        };
    }

    private void breakBedrock(Block block, Player player) {
        Location loc = block.getLocation();
        block.setType(Material.AIR);
        block.getWorld().dropItemNaturally(loc, new ItemStack(Material.BEDROCK, 1));
        block.getWorld().playSound(loc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
    }
}
