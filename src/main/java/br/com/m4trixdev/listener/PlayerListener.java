package br.com.m4trixdev.listener;

import br.com.m4trixdev.Main;
import br.com.m4trixdev.model.EventState;
import br.com.m4trixdev.model.Plot;
import br.com.m4trixdev.model.VoteItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> warnCooldown = new HashMap<>();

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent event) {
        EventState st = plugin.getEventManager().getState();
        if (st != EventState.BUILDING && st != EventState.PHASE_1) return;

        Location from = event.getFrom();
        Location to   = event.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX()
         && from.getBlockY() == to.getBlockY()
         && from.getBlockZ() == to.getBlockZ()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("buildbattle.admin")) return;

        Plot plot = plugin.getPlotManager().plotOf(player.getUniqueId());
        if (plot == null) return;

        if (!plugin.getPlotManager().isInsidePlot(plot, to)) {
            event.setTo(plugin.getEventManager().spawnOnPlot(plot.getCenter()));
            warnOutside(player);
        }
    }

    private void warnOutside(Player player) {
        long now = System.currentTimeMillis();
        Long last = warnCooldown.get(player.getUniqueId());
        if (last != null && now - last < 2500) return;
        warnCooldown.put(player.getUniqueId(), now);
        player.sendMessage(plugin.getConfigManager().fmt(
            plugin.getConfigManager().getMsgOutside()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent event) {
        EventState st = plugin.getEventManager().getState();
        if (st == EventState.WAITING || st == EventState.ENDED) return;
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (plugin.getEventManager().getState() != EventState.VOTING) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        var item = event.getItem();
        if (item == null) return;

        VoteItem vi = VoteItem.fromMaterial(item.getType());
        if (vi == null) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        if (!player.hasPermission("buildbattle.admin")) {
            player.sendMessage(plugin.getConfigManager().fmt(
                plugin.getConfigManager().getMsgNoPermission()));
            return;
        }

        Plot current = plugin.getEventManager().currentVotingPlot();
        if (current == null) return;

        if (current.isOwner(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().fmt(
                plugin.getConfigManager().getMsgOwnPlot()));
            return;
        }

        if (vi == VoteItem.DELETE) {
            plugin.getPlotManager().clearBlocks(current);
            Bukkit.broadcastMessage(plugin.getConfigManager().fmt(
                plugin.getConfigManager().getMsgDeleted().replace("%player%", current.getOwnerName())));
            return;
        }

        boolean ok = plugin.getVoteManager().vote(player.getUniqueId(), current, vi.getScore());
        if (ok) {
            player.sendMessage(plugin.getConfigManager().fmt(
                plugin.getConfigManager().getMsgVoteOk()
                    .replace("%score%", String.valueOf(vi.getScore()))));
        } else {
            player.sendMessage(plugin.getConfigManager().fmt(
                plugin.getConfigManager().getMsgAlreadyVoted()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (plugin.getEventManager().getState() != EventState.VOTING) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getEventManager().getState() == EventState.VOTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        warnCooldown.remove(player.getUniqueId());
        plugin.getScoreboardManager().onQuit(player);

        EventState st = plugin.getEventManager().getState();
        if (st != EventState.WAITING && st != EventState.ENDED) {
            plugin.getEventManager().removeParticipant(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getScoreboardManager().onJoin(event.getPlayer());
    }
}
