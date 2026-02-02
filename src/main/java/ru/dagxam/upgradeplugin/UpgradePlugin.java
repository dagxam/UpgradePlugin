package ru.dagxam.upgradeplugin;

import org.bukkit.plugin.java.JavaPlugin;
import ru.dagxam.upgradeplugin.commands.GiveBookCommand;
import ru.dagxam.upgradeplugin.commands.UpgradeCommand;
import ru.dagxam.upgradeplugin.config.Messages;
import ru.dagxam.upgradeplugin.config.PluginConfig;
import ru.dagxam.upgradeplugin.listeners.AnvilListener;
import ru.dagxam.upgradeplugin.listeners.BlockBreakListener;
import ru.dagxam.upgradeplugin.listeners.VillagerListener;
import ru.dagxam.upgradeplugin.upgrade.UpgradeManager;

public final class UpgradePlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private UpgradeManager upgradeManager;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        this.messages = new Messages(this);
        this.pluginConfig = new PluginConfig(this);
        this.upgradeManager = new UpgradeManager(this, pluginConfig);

        // /giveupgradebook (совместимость)
        if (getCommand("giveupgradebook") != null) {
            GiveBookCommand give = new GiveBookCommand(upgradeManager, messages);
            getCommand("giveupgradebook").setExecutor(give);
            getCommand("giveupgradebook").setTabCompleter(give);
        } else {
            getLogger().severe("Команда giveupgradebook не найдена в plugin.yml");
        }

        // /upgrade (основная команда)
        if (getCommand("upgrade") != null) {
            UpgradeCommand upgrade = new UpgradeCommand(this, upgradeManager, messages);
            getCommand("upgrade").setExecutor(upgrade);
            getCommand("upgrade").setTabCompleter(upgrade);
        } else {
            getLogger().severe("Команда upgrade не найдена в plugin.yml");
        }

        // Слушатели
        getServer().getPluginManager().registerEvents(new AnvilListener(this, upgradeManager), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this, pluginConfig, upgradeManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this, pluginConfig, upgradeManager), this);

        getLogger().info("UpgradePlugin успешно включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("UpgradePlugin выключен.");
    }

    /**
     * Перезагрузка config.yml + messages.yml без рестарта.
     * Вызывается из /upgrade reload.
     */
    public void reloadAll() {
        // reloadConfig() вызовется внутри pluginConfig.reload(), но делаем явно для читабельности
        reloadConfig();
        if (pluginConfig != null) pluginConfig.reload();
        if (messages != null) messages.reload();
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public Messages getMessages() {
        return messages;
    }
}
