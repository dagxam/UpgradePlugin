package ru.dagxam.upgradeplugin;

import org.bukkit.plugin.java.JavaPlugin;
import ru.dagxam.upgradeplugin.commands.GiveBookCommand;
import ru.dagxam.upgradeplugin.listeners.AnvilListener;
import ru.dagxam.upgradeplugin.listeners.BlockBreakListener; // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ
import ru.dagxam.upgradeplugin.listeners.VillagerListener;

public final class UpgradePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Инициализация ItemManager...");
        
        getCommand("giveupgradebook").setExecutor(new GiveBookCommand());

        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        // --- ДОБАВЬТЕ ЭТУ СТРОКУ ---
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this); 

        getLogger().info("UpgradePlugin успешно включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("UpgradePlugin выключен.");
    }
}
