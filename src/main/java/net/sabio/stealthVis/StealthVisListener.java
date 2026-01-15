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

public class StealthVisListener implements Listener {
    private final StealthVis plugin;
    public StealthVisListener(StealthVis plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInvisibilityChange(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        boolean isInvis = event.getNewEffect() != null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY);
        boolean lostInvis = event.getOldEffect() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY) && event.getNewEffect() == null;
        if (isInvis) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.hidePlayer(plugin, player);
            }
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " left the game");
        } else if (lostInvis) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " joined the game");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        String message = event.getDeathMessage();
        if (message == null) return;
        if (victim.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            message = message.replace(victim.getName(), "[?]");
        }
        if (killer != null && killer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            message = message.replace(killer.getName(), "[?]");
        }
        event.setDeathMessage(message);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            event.setJoinMessage(null);
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.hidePlayer(plugin, event.getPlayer());
            }
        }
    }
}
