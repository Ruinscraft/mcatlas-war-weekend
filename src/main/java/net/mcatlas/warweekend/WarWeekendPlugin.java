package net.mcatlas.warweekend;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WarWeekendPlugin extends JavaPlugin {

    private MySQLWarStorage mySQLWarStorage;
    private WarManager warManager;
    private WarTask warTask;
    private ScoreKeeperTask scoreKeeperTask;
    private ChooseTeamGUI chooseTeamGUI;

    public MySQLWarStorage getMySQLWarStorage() {
        return mySQLWarStorage;
    }

    public WarManager getWarManager() {
        return warManager;
    }

    public ScoreKeeperTask getScoreKeeperTask() {
        return scoreKeeperTask;
    }

    public ChooseTeamGUI getChooseTeamGUI() {
        return chooseTeamGUI;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("Towny") == null) {
            getLogger().warning("Towny not found");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        chooseTeamGUI = new ChooseTeamGUI(this);

        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(chooseTeamGUI, this);
        getServer().getPluginManager().registerEvents(new HelmetListener(this), this);
        getServer().getPluginManager().registerEvents(new PVPListener(this), this);
        getServer().getPluginManager().registerEvents(new CustomItemListener(this), this);

        getCommand("joinwar").setExecutor(new JoinWarCommand(this));

        String host = getConfig().getString("mysql.host");
        int port = getConfig().getInt("mysql.port");
        String database = getConfig().getString("mysql.database");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        mySQLWarStorage = new MySQLWarStorage(host, port, database, username, password);
        mySQLWarStorage.createTables();

        warManager = new WarManager();
        warManager.loadWarTownsFromStorage(this).join();

        warTask = new WarTask(this);
        getServer().getScheduler().runTaskTimer(this, warTask, 20L * 30L, 20L * 30L);

        scoreKeeperTask = new ScoreKeeperTask(this);
        getServer().getScheduler().runTaskTimer(this, scoreKeeperTask, 20L * 5L, 20L * 5L);
        getServer().getPluginManager().registerEvents(scoreKeeperTask, this);
        getCommand("warstats").setExecutor(scoreKeeperTask);

        getServer().getScheduler().runTaskTimer(this, new ActionBarTask(this), 20L * 60L, 20L * 60L);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            player.sendTitle(ChatColor.GOLD + "WEEKEND WAR", ChatColor.YELLOW + "MELONS VS PUMPKINS");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "============ EMERGENCY ============");
            player.sendMessage(ChatColor.YELLOW + "MELONS VS PUMPKINS WAR");
            player.sendMessage(ChatColor.YELLOW + "JOIN IMMEDIATELY WITH /joinwar");
            player.sendMessage(ChatColor.YELLOW + "PLEASE READ HERE: https://ruinscraft.com/threads/mcatlas-weekend-war.12590/");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "============ EMERGENCY ============");
        }
    }

    @Override
    public void onDisable() {

    }

}
