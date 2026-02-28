package br.com.m4trixdev;

import br.com.m4trixdev.command.BBCommand;
import br.com.m4trixdev.config.ConfigManager;
import br.com.m4trixdev.data.DataManager;
import br.com.m4trixdev.listener.BlockListener;
import br.com.m4trixdev.listener.PlayerListener;
import br.com.m4trixdev.manager.*;
import br.com.m4trixdev.model.EventState;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ConfigManager configManager;
    private DataManager dataManager;
    private PlotManager plotManager;
    private VoteManager voteManager;
    private ScoreManager scoreManager;
    private EventManager eventManager;
    private BBScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        if (!configManager.load()) {
            getLogger().severe("Falha ao carregar config.yml. Plugin desativado.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dataManager      = new DataManager(this);
        dataManager.load();

        plotManager      = new PlotManager(this);
        voteManager      = new VoteManager();
        scoreManager     = new ScoreManager();
        eventManager     = new EventManager(this);
        scoreboardManager = new BBScoreboardManager(this);

        var cmd = getCommand("bb");
        if (cmd != null) {
            var handler = new BBCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        scoreboardManager.start();

        getLogger().info("BuildBattle ativado.");
    }

    @Override
    public void onDisable() {
        if (eventManager != null && eventManager.getState() != EventState.WAITING) {
            eventManager.stop();
        }
        if (scoreboardManager != null) scoreboardManager.stop();
        getLogger().info("BuildBattle desativado.");
    }

    public ConfigManager getConfigManager()       { return configManager; }
    public DataManager getDataManager()           { return dataManager; }
    public PlotManager getPlotManager()           { return plotManager; }
    public VoteManager getVoteManager()           { return voteManager; }
    public ScoreManager getScoreManager()         { return scoreManager; }
    public EventManager getEventManager()         { return eventManager; }
    public BBScoreboardManager getScoreboardManager() { return scoreboardManager; }
}
