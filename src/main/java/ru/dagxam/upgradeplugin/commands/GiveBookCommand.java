package ru.dagxam.upgradeplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.dagxam.upgradeplugin.config.Messages;
import ru.dagxam.upgradeplugin.upgrade.UpgradeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GiveBookCommand implements CommandExecutor, TabCompleter {

    private final UpgradeManager upgradeManager;
    private final Messages messages;

    public GiveBookCommand(UpgradeManager upgradeManager, Messages messages) {
        this.upgradeManager = upgradeManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("upgradeplugin.givebook")) {
            sender.sendMessage(messages.get("errors.no_permission", "&cУ вас нет прав."));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(messages.get("commands.givebook.usage", "&cИспользование: /" + label + " <имя> <количество>")
                    .replace("{label}", label));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(messages.get("errors.player_not_found", "&cИгрок '{player}' не найден (не в сети).")
                    .replace("{player}", args[0]));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.get("errors.invalid_number", "&c'{value}' не является допустимым числом.")
                    .replace("{value}", args[1]));
            return true;
        }

        if (amount < 1) {
            sender.sendMessage(messages.get("errors.amount_min", "&cКоличество должно быть 1 или больше."));
            return true;
        }

        ItemStack book = upgradeManager.createUpgradeBook();
        book.setAmount(amount);

        target.getInventory().addItem(book);

        target.sendMessage(messages.get("commands.givebook.received", "&aВы получили {amount} Книг(у) Улучшения!")
                .replace("{amount}", String.valueOf(amount)));

        if (sender != target) {
            sender.sendMessage(messages.get("commands.givebook.given", "&aВы выдали {amount} Книг(у) Улучшения игроку {player}.")
                    .replace("{amount}", String.valueOf(amount))
                    .replace("{player}", target.getName()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> out = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) out.add(p.getName());
            }
            return out;
        }
        if (args.length == 2) {
            return Arrays.asList("1", "2", "5", "10", "16", "32", "64");
        }
        return Collections.emptyList();
    }
}
