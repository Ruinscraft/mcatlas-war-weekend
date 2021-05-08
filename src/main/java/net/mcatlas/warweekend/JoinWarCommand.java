package net.mcatlas.warweekend;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JoinWarCommand implements CommandExecutor {

    private WarWeekendPlugin warWeekendPlugin;
    private Map<UUID, Long> lastUsed;

    public JoinWarCommand(WarWeekendPlugin warWeekendPlugin) {
        this.warWeekendPlugin = warWeekendPlugin;
        lastUsed = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (lastUsed.containsKey(player.getUniqueId())) {
            if (lastUsed.get(player.getUniqueId()) + TimeUnit.MINUTES.toMillis(10) > System.currentTimeMillis()) {
                player.sendMessage(ChatColor.RED + "You must wait to use this command again.");
                return false;
            }
        }

        warWeekendPlugin.getChooseTeamGUI().open(player);
        lastUsed.put(player.getUniqueId(), System.currentTimeMillis());

        return true;
    }

}
