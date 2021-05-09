package net.mcatlas.warweekend;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomItemListener implements Listener {

    private Map<UUID, Material> doubleClick = new HashMap<>();
    private Set<UUID> nautilusShellHitCooldown = new HashSet<>();
    private Set<UUID> nautilusShellUseCooldown = new HashSet<>();
    private Set<UUID> chorusFruitHitCooldown = new HashSet<>();
    private Set<UUID> chorusFruitUseCooldown = new HashSet<>();

    private static int PARTICLE_COUNT = 20;

    private Random random = new Random();

    private WarWeekendPlugin plugin;

    public CustomItemListener(WarWeekendPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // if player isnt on a team, return
        WarManager warManager = plugin.getWarManager();
        if (warManager.getTeam(player) == null) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) return;
        Material material = event.getItem().getType();
        switch (material) {
            case MUSIC_DISC_11:
                if (doubleClick.get(uuid) == Material.MUSIC_DISC_11) {
                    boolean success = disc11Action(player);
                    if (success) {
                        removeOneItemFromHand(event);
                    }
                } else {
                    doubleClick.put(uuid, material);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        doubleClick.remove(uuid, material);
                    }, 5);
                }
                return;
            case MUSIC_DISC_FAR:
                if (doubleClick.get(uuid) == Material.MUSIC_DISC_FAR) {
                    boolean success = discFarAction(player);
                    if (success) {
                        removeOneItemFromHand(event);
                    }
                } else {
                    doubleClick.put(uuid, material);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        doubleClick.remove(uuid, material);
                    }, 5);
                }
                return;
            case NAUTILUS_SHELL:
                if (doubleClick.get(uuid) == Material.NAUTILUS_SHELL) {
                    if (nautilusShellUseCooldown.contains(uuid)) {
                        player.sendMessage(ChatColor.RED + "This item is cooling down.");
                        return;
                    }
                    boolean success = nautilusShellAction(player);
                    if (success) {
                        removeOneItemFromHand(event);
                        nautilusShellUseCooldown.add(uuid);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            nautilusShellUseCooldown.remove(uuid);
                        }, 20 * 30);
                    }
                } else {
                    doubleClick.put(uuid, material);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        doubleClick.remove(uuid, material);
                    }, 5);
                }
                return;
            case CHORUS_FRUIT:
                if (doubleClick.get(uuid) == Material.CHORUS_FRUIT) {
                    if (chorusFruitUseCooldown.contains(uuid)) {
                        player.sendMessage(ChatColor.RED + "This item is cooling down.");
                        return;
                    }
                    boolean success = chorusFruitAction(player);
                    if (success) {
                        removeOneItemFromHand(event);
                        chorusFruitUseCooldown.add(uuid);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            chorusFruitUseCooldown.remove(uuid);
                        }, 20 * 30);
                    }
                } else {
                    doubleClick.put(uuid, material);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        doubleClick.remove(uuid, material);
                    }, 5);
                }
                return;
        }
    }

    public void removeOneItemFromHand(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null || event.getItem().getType() == Material.AIR) {
            return;
        }
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }

    // return true if succeed
    public boolean disc11Action(Player actor) {
        TownBlock block = TownyAPI.getInstance().getTownBlock(actor.getLocation());
        if (block == null) {
            actor.sendMessage(ChatColor.RED + "You're not in a town!");
            return false;
        }

        WarManager warManager = plugin.getWarManager();
        Player opposerToTeleport = warManager.getClosestOpposerInTown(actor);
        if (opposerToTeleport == null) {
            actor.sendMessage(ChatColor.RED + "No members of other teams in the Town!");
            return false;
        }

        opposerToTeleport.teleport(actor.getLocation());

        opposerToTeleport.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "TELEPORTED");
        opposerToTeleport.sendMessage(ChatColor.GRAY + actor.getName() + " brought you to them with Disc 11.");
        actor.getWorld().playSound(actor.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        actor.sendMessage(ChatColor.GRAY + "Successfully teleported " + opposerToTeleport.getName() + " to you.");
        return true;
    }

    // return true if succeed
    public boolean discFarAction(Player actor) {
        TownBlock block = TownyAPI.getInstance().getTownBlock(actor.getLocation());
        if (block == null) {
            actor.sendMessage(ChatColor.RED + "You're not in a town!");
            return false;
        }

        WarManager warManager = plugin.getWarManager();
        Player opposerToTeleport = warManager.getFarthestOpposerInTown(actor);
        if (opposerToTeleport == null) {
            actor.sendMessage(ChatColor.RED + "No members of other teams in the Town!");
            return false;
        }

        opposerToTeleport.teleport(actor.getLocation());

        opposerToTeleport.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "TELEPORTED");
        opposerToTeleport.sendMessage(ChatColor.GRAY + actor.getName() + " brought you to them with Disc Far.");
        actor.getWorld().playSound(actor.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        actor.sendMessage(ChatColor.GRAY + "Successfully teleported " + opposerToTeleport.getName() + " to you.");
        return true;
    }

    // return true if succeed
    public boolean nautilusShellAction(Player actor) {
        TownBlock block = TownyAPI.getInstance().getTownBlock(actor.getLocation());
        if (block == null) {
            actor.sendMessage(ChatColor.RED + "You're not in a town!");
            return false;
        }

        WarManager warManager = plugin.getWarManager();
        Set<WarPlayer> opposersInTown = warManager.getOpposingPlayersInTown(actor);

        boolean hitAnyPlayers = false;
        int hit = 0;
        for (WarPlayer warPlayer : opposersInTown) {
            Player opposer = warPlayer.getPlayer();

            opposer.playSound(opposer.getLocation(), Sound.ENTITY_TURTLE_HURT, 3, 1);
            double distance = actor.getLocation().distance(opposer.getLocation());

            if (distance > 20) continue;

            if (nautilusShellHitCooldown.contains(opposer.getUniqueId())) continue;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                opposer.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "SHELL ATTACK");
                opposer.sendMessage(ChatColor.AQUA + "You've been hurt by a nautilus shell used by " + actor.getName());
                opposer.playSound(opposer.getLocation(), Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1, 1);
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 4);
                opposer.getWorld().spawnParticle(Particle.REDSTONE, opposer.getEyeLocation(), 1, dustOptions);
                double health = opposer.getHealth() - 10;
                if (health <= 0) health = 0;
                opposer.setHealth(health);
            }, (int) distance);

            nautilusShellHitCooldown.add(opposer.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                nautilusShellHitCooldown.remove(opposer.getUniqueId());
            }, 20 * 30);

            hit++;
            hitAnyPlayers = true;
        }

        World world = actor.getWorld();
        Location actorLocation = actor.getLocation();

        for (int time = 0; time < 20; time++) {
            final int solidTime = time;
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                double rand = random.nextDouble();
                for (int height = 0; height < 3; height++) {
                    for (int i = 0; i < solidTime * 2; i++) {
                        double angle = ((2 * Math.PI) * ((double) i / (double) solidTime)) - rand;
                        Location point = actorLocation.clone().add(solidTime * Math.sin(angle), (height - 1) * 10, solidTime * Math.cos(angle));

                        Particle.DustOptions options = new Particle.DustOptions(Color.TEAL, 5);

                        world.spawnParticle(Particle.REDSTONE, point, 1, options);
                    }
                }
            }, time);
        }

        actor.sendMessage(ChatColor.AQUA + "Hit " + hit + " players with the nautilus shell.");
        return hitAnyPlayers;
    }

    // return true if succeed
    public boolean chorusFruitAction(Player actor) {
        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(actor.getLocation());
        if (townBlock == null) {
            actor.sendMessage(ChatColor.RED + "You're not in a town!");
            return false;
        }

        Player target = getPlayerInLineOfSight(actor);
        if (target == null) {
            actor.sendMessage(ChatColor.RED + "No players in your line of sight.");
            return false;
        }

        WarManager warManager = plugin.getWarManager();
        if (!warManager.isInSameTown(actor, target)) {
            actor.sendMessage(ChatColor.RED + "You're not in the same town as them!");
            return false;
        }

        if (warManager.onSameTeam(actor, target)) {
            actor.sendMessage(ChatColor.RED + "This player is on your team or isn't in the war.");
            return false;
        }

        if (chorusFruitHitCooldown.contains(target.getUniqueId())) {
            actor.sendMessage(ChatColor.RED + "This player was hit by chorus fruit too recently!");
            return false;
        }

        int addX = random.nextInt(24) - 12;
        int addZ = random.nextInt(24) - 12;

        Location location = target.getLocation().clone().add(addX, 0, addZ);
        Block block = target.getWorld().getHighestBlockAt(location);
        target.teleport(block.getLocation().clone().add(0, 1, 0));
        target.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "TELEPORTED");
        target.sendMessage(ChatColor.GRAY + actor.getName() + " used the chorus fruit effect on you.");

        chorusFruitHitCooldown.add(target.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            chorusFruitHitCooldown.remove(target.getUniqueId());
        }, 20 * 30);

        actor.sendMessage(ChatColor.DARK_PURPLE + "Used the chorus fruit on " + target.getName() + ".");
        return true;
    }

    public Player getPlayerInLineOfSight(Player observer) {
        Location observerPos = observer.getEyeLocation();
        Vector3D observerDir = new Vector3D(observerPos.getDirection());

        Vector3D observerStart = new Vector3D(observerPos);
        Vector3D observerEnd = observerStart.add(observerDir.multiply(50));

        Player hit = null;

        // Get nearby entities
        for (Player target : observer.getWorld().getPlayers()) {
            // Bounding box of the given player
            Vector3D targetPos = new Vector3D(target.getLocation());
            Vector3D minimum = targetPos.add(-0.5, 0, -0.5);
            Vector3D maximum = targetPos.add(0.5, 1.67, 0.5);

            if (target != observer && hasIntersection(observerStart, observerEnd, minimum, maximum)) {
                if (hit == null ||
                        hit.getLocation().distanceSquared(observerPos) >
                                target.getLocation().distanceSquared(observerPos)) {

                    hit = target;
                }
            }
        }

        return hit;
    }

    // Source:
    // [url]http://www.gamedev.net/topic/338987-aabb---line-segment-intersection-test/[/url]
    private boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
        final double epsilon = 0.0001f;

        Vector3D d = p2.subtract(p1).multiply(0.5);
        Vector3D e = max.subtract(min).multiply(0.5);
        Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
        Vector3D ad = d.abs();

        if (Math.abs(c.x) > e.x + ad.x)
            return false;
        if (Math.abs(c.y) > e.y + ad.y)
            return false;
        if (Math.abs(c.z) > e.z + ad.z)
            return false;

        if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon)
            return false;
        if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon)
            return false;
        if (Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon)
            return false;

        return true;
    }

}
