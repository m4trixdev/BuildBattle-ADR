package br.com.m4trixdev.listener;

import br.com.m4trixdev.Main;
import br.com.m4trixdev.model.EventState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockListener implements Listener {

    private final Main plugin;

    public BlockListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.BARRIER || type == Material.QUARTZ_BLOCK) {
            event.setCancelled(true);
            return;
        }

        EventState st = plugin.getEventManager().getState();

        if (st == EventState.WAITING || st == EventState.ENDED) return;

        Player player = event.getPlayer();

        if (plugin.getPlotManager().isSpawnBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (player.hasPermission("buildbattle.admin")) return;

        if (st != EventState.BUILDING) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getPlotManager().playerPlaced(player.getUniqueId(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        plugin.getPlotManager().untrackPlace(player.getUniqueId(), event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        EventState st = plugin.getEventManager().getState();

        if (st == EventState.WAITING || st == EventState.ENDED) return;

        Player player = event.getPlayer();
        if (player.hasPermission("buildbattle.admin")) return;

        if (st != EventState.BUILDING) {
            event.setCancelled(true);
            return;
        }

        Material mat = event.getBlock().getType();

        if (plugin.getConfigManager().isBlockTnt()   && mat == Material.TNT)         { event.setCancelled(true); return; }
        if (plugin.getConfigManager().isBlockLava()  && mat == Material.LAVA)        { event.setCancelled(true); return; }
        if (plugin.getConfigManager().isBlockWater() && mat == Material.WATER)       { event.setCancelled(true); return; }
        if (mat == Material.QUARTZ_BLOCK || mat == Material.BARRIER)                 { event.setCancelled(true); return; }

        Material spawnMat = plugin.getPlotManager().getSpawnBlockMaterial();
        if (spawnMat != null && mat == spawnMat) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getPlotManager().isInsideOwnPlot(player.getUniqueId(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        plugin.getPlotManager().trackPlace(player.getUniqueId(), event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        EventState st = plugin.getEventManager().getState();
        if (st == EventState.BUILDING || st == EventState.PHASE_1 || st == EventState.VOTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        EventState st = plugin.getEventManager().getState();
        if (st == EventState.BUILDING || st == EventState.PHASE_1 || st == EventState.VOTING) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        EventState st = plugin.getEventManager().getState();
        if (st == EventState.BUILDING || st == EventState.PHASE_1 || st == EventState.VOTING) {
            event.blockList().clear();
        }
    }
}
