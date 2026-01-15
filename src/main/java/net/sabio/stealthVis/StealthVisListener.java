package net.sabio.stealthVis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StealthVisListener implements Listener {
    private final StealthVis plugin;
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Set<UUID> pendingJoinMessage = new HashSet<>();
    public StealthVisListener(StealthVis plugin) {
        this.plugin = plugin;
        setupStealthTeam();
    }

    private void setupStealthTeam() {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("stealth");
        if (team == null) team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("stealth");
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.setCanSeeFriendlyInvisibles(false);
    }

    @EventHandler
    public void onInvisibilityChange(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        boolean hasInvis = player.hasPotionEffect(PotionEffectType.INVISIBILITY);
        UUID uuid = player.getUniqueId();
        if (hasInvis && !hiddenPlayers.contains(uuid)) {
            hiddenPlayers.add(uuid);
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("stealth").addEntry(player.getName());
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " left the game");
        } else if (!hasInvis && hiddenPlayers.contains(uuid)) {
            hiddenPlayers.remove(uuid);
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("stealth").removeEntry(player.getName());
            if (pendingJoinMessage.contains(uuid)) {
                scheduleFakeJoin(player);
            } else {
                Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " joined the game");
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        String message = event.getDeathMessage();
        if (message == null) return;
        if (hiddenPlayers.contains(victim.getUniqueId())) {
            message = message.replaceAll("\\b" + victim.getName() + "\\b", "[?]");
            pendingJoinMessage.add(victim.getUniqueId());
        }
        Player killer = victim.getKiller();
        if (killer != null && hiddenPlayers.contains(killer.getUniqueId())) {
            message = message.replaceAll("\\b" + killer.getName() + "\\b", "[?]");
        }
        event.setDeathMessage(message);
    }

    private void scheduleFakeJoin(Player player) {
        UUID uuid = player.getUniqueId();
        long delaySeconds = ThreadLocalRandom.current().nextLong(40, 121);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && pendingJoinMessage.contains(uuid)) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " joined the game");
                    pendingJoinMessage.remove(uuid);
                }
            }
        }.runTaskLater(plugin, delaySeconds * 20);
    }
}
