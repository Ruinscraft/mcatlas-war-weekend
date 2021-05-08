package net.mcatlas.warweekend;

import org.bukkit.entity.Player;

import java.util.Objects;

public class WarPlayer {

    private Player player;
    private WarTeam warTeam;

    public WarPlayer(Player player, WarTeam warTeam) {
        this.player = player;
        this.warTeam = warTeam;
    }

    public Player getPlayer() {
        return player;
    }

    public WarTeam getWarTeam() {
        return warTeam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarPlayer warPlayer = (WarPlayer) o;
        return Objects.equals(player, warPlayer.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }

}
