package net.mcatlas.warweekend;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

public class ActionBarTask implements Runnable {

    private WarWeekendPlugin warWeekendPlugin;

    public ActionBarTask(WarWeekendPlugin warWeekendPlugin) {
        this.warWeekendPlugin = warWeekendPlugin;
    }

    @Override
    public void run() {
        for (WarPlayer warPlayer : warWeekendPlugin.getWarManager().getParticipants()) {
            TextComponent message = new TextComponent(ChatColor.YELLOW + "YOU ARE: " + warPlayer.getWarTeam().getColor() + warPlayer.getWarTeam().getStylizedName());
            warPlayer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
        }
    }

}
