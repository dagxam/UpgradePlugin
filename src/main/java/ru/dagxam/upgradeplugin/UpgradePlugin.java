// ИЗМЕНЕНО
package ru.dagxam.upgradeplugin;

import org.bukkit.plugin.java.JavaPlugin;
// ИЗМЕНЕНО
import ru.dagxam.upgradeplugin.commands.GiveBookCommand;
import ru.dagxam.upgradeplugin.listeners.AnvilListener;
import ru.dagxam.upgradeplugin.listeners.VillagerListener;

public final class UpgradePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Инициализация ItemManager...");
        
        // 1. Регистрация команд
        getCommand("giveupgradebook").setExecutor(new GiveBookCommand());

        // 2. Регистрация слушателей событий
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this); // Передаем 'this'
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this); // Передаем 'this'

        getLogger().info("UpgradePlugin успешно включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("UpgradePlugin выключен.");
    }
}
