// ИЗМЕНЕНО
package ru.dagxam.upgradeplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
// ИЗМЕНЕНО
import ru.dagxam.upgradeplugin.items.ItemManager;

public class GiveBookCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("upgradeplugin.givebook")) {
                ItemStack book = ItemManager.createUpgradeBook();
                player.getInventory().addItem(book);
                player.sendMessage("§aВы получили Книгу Улучшения!");
            } else {
                player.sendMessage("§cУ вас нет прав.");
            }
            return true;
        }
        return false;
    }
}
