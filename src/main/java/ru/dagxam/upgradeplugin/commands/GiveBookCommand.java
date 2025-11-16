package ru.dagxam.upgradeplugin.commands;

import org.bukkit.Bukkit; // <-- НУЖЕН НОВЫЙ ИМПОРТ
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.dagxam.upgradeplugin.items.ItemManager;

public class GiveBookCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // 1. Проверяем, что у того, кто ввел команду, есть права
        if (!sender.hasPermission("upgradeplugin.givebook")) {
            sender.sendMessage("§cУ вас нет прав.");
            return true;
        }

        // 2. Проверяем, что введено 2 аргумента: [имя] и [количество]
        if (args.length != 2) {
            sender.sendMessage("§cИспользование: /" + label + " <имя> <количество>");
            return true;
        }

        // 3. Пытаемся найти игрока по первому аргументу
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cИгрок '" + args[0] + "' не найден (не в сети).");
            return true;
        }

        // 4. Пытаемся получить количество из второго аргумента
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c'" + args[1] + "' не является допустимым числом.");
            return true;
        }

        if (amount < 1) {
            sender.sendMessage("§cКоличество должно быть 1 или больше.");
            return true;
        }

        // 5. Все проверки пройдены. Создаем книгу и устанавливаем количество
        ItemStack book = ItemManager.createUpgradeBook();
        book.setAmount(amount);

        // 6. Выдаем предмет
        target.getInventory().addItem(book);

        // 7. Отправляем сообщения об успехе
        target.sendMessage("§aВы получили " + amount + " Книг(у) Улучшения!");
        
        // Отправляем сообщение тому, кто ввел команду (если это не тот же игрок)
        if (sender != target) {
            sender.sendMessage("§aВы выдали " + amount + " Книг(у) Улучшения игроку " + target.getName() + ".");
        }
        
        return true;
    }
}
