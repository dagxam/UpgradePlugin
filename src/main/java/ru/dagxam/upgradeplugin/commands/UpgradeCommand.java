package ru.dagxam.upgradeplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.dagxam.upgradeplugin.UpgradePlugin;
import ru.dagxam.upgradeplugin.config.Messages;
import ru.dagxam.upgradeplugin.upgrade.UpgradeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpgradeCommand implements CommandExecutor, TabCompleter {

    private final UpgradePlugin plugin;
    private final UpgradeManager upgradeManager;
    private final Messages messages;

    public UpgradeCommand(UpgradePlugin plugin, UpgradeManager upgradeManager, Messages messages) {
        this.plugin = plugin;
        this.upgradeManager = upgradeManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(messages.get("commands.upgrade.usage", "&eИспользование: /upgrade <give|reload>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("upgradeplugin.reload")) {
                    sender.sendMessage(messages.get("errors.no_permission", "&cУ вас нет прав."));
                    return true;
                }

                try {
                    plugin.reloadAll();
                    sender.sendMessage(messages.get("commands.upgrade.reload_ok", "&aКонфиг перезагружен."));
                } catch (Exception ex) {
                    sender.sendMessage(messages.get("commands.upgrade.reload_fail", "&cОшибка перезагрузки конфига. Смотрите консоль."));
                    plugin.getLogger().severe("Ошибка /upgrade reload: " + ex.getMessage());
                    ex.printStackTrace();
                }
                return true;
            }
            case "give" -> {
                if (!sender.hasPermission("upgradeplugin.givebook")) {
                    sender.sendMessage(messages.get("errors.no_permission", "&cУ вас нет прав."));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(messages.get("commands.upgrade.give_usage", "&cИспользование: /upgrade give <игрок> [количество]"));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(messages.get("errors.player_not_found", "&cИгрок не найден или не в сети.").replace("{player}", args[1]));
                    return true;
                }

                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(messages.get("errors.invalid_number", "&cНеверное число.").replace("{value}", args[2]));
                        return true;
                    }
                    if (amount < 1) {
                        sender.sendMessage(messages.get("errors.amount_min", "&cКоличество должно быть 1 или больше."));
                        return true;
                    }
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
            default -> {
                sender.sendMessage(messages.get("commands.upgrade.usage", "&eИспользование: /upgrade <give|reload>"));
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("give", "reload"));
            subs.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return subs;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String prefix = args[1].toLowerCase();
            List<String> out = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) out.add(p.getName());
            }
            return out;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("1", "2", "5", "10", "16", "32", "64");
        }
        return Collections.emptyList();
    }
}
