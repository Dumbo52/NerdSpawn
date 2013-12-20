package nu.nerd.nerdspawn;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class NerdSpawnListener implements Listener {
    private final NerdSpawn plugin;

    /**
     * Map from player name to most recent death location. Does not survive
     * restarts, sorry.
     */
    private HashMap<String, Location> deathLocations = new HashMap<String, Location>();

    public NerdSpawnListener(NerdSpawn instance) {
        plugin = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().teleport(plugin.getSpawnLocation());
            if (plugin.getConfig().getBoolean("show-join-message")) {
                String message = plugin.getConfig().getString("join-message");
                message = message.replaceAll("%u", event.getPlayer().getName());
                for (ChatColor c : ChatColor.values())
                    message = message.replaceAll(("&" + c.getChar()), c.toString());

                plugin.getServer().broadcastMessage(message);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        deathLocations.put(event.getEntity().getName(), event.getEntity().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.isBedSpawn()) {
            boolean goToSpawn = false;
            if (plugin.getConfig().getBoolean("allow-bed-spawn")) {
                if (plugin.getConfig().getBoolean("check-bed-radius")) {
                    Player player = event.getPlayer();
                    Location deathLocation = deathLocations.get(player.getName());
                    if (deathLocation != null) {
                        double bedRadius = plugin.getConfig().getDouble("bed-radius", 15);
                        if (player.getBedSpawnLocation().distanceSquared(deathLocation) < bedRadius * bedRadius) {
                            player.sendMessage(ChatColor.GOLD + "You died too close to your bed. Sending you back to spawn.");
                            goToSpawn = true;
                        }
                    }
                }
            } else {
                goToSpawn = true;
            }

            if (goToSpawn) {
                event.setRespawnLocation(plugin.getSpawnLocation());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getFrom().getWorld().getEnvironment() == Environment.THE_END && !plugin.getConfig().getBoolean("allow-bed-spawn")) {
            event.setTo(plugin.getSpawnLocation());
        }
    }
}
