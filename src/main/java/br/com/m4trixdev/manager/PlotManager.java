package br.com.m4trixdev.manager;

import br.com.m4trixdev.Main;
import br.com.m4trixdev.model.Plot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotManager {

    private final Main plugin;
    private final List<Plot> plots = new ArrayList<>();
    private final Map<UUID, Plot> byPlayer = new HashMap<>();
    private final Map<UUID, Set<String>> placedBlocks = new HashMap<>();
    private final Set<String> spawnBlockKeys = new HashSet<>();
    private Material spawnBlockMaterial = null;

    public PlotManager(Main plugin) {
        this.plugin = plugin;
    }

    public List<Location> scanForPlots(Location pos1, Location pos2, Material material) {
        List<Location> found = new ArrayList<>();

        if (pos1 == null || pos2 == null) return found;
        if (pos1.getWorld() == null) return found;

        World world = pos1.getWorld();
        String w2 = pos2.getWorld() != null ? pos2.getWorld().getName() : "";
        if (!world.getName().equals(w2)) return found;

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;

        plugin.getLogger().info("[BuildBattle] Escaneando X[" + minX + ".." + maxX
            + "] Z[" + minZ + ".." + maxZ
            + "] Y[" + minY + ".." + maxY
            + "] material=" + material.name());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (world.getBlockAt(x, y, z).getType() == material) {
                        found.add(new Location(world, x, y, z));
                    }
                }
            }
        }

        plugin.getLogger().info("[BuildBattle] Plots encontrados: " + found.size());
        return found;
    }

    public void buildPlots(List<Location> centers, Material material) {
        plots.clear();
        byPlayer.clear();
        placedBlocks.clear();
        spawnBlockKeys.clear();
        spawnBlockMaterial = material;

        for (int i = 0; i < centers.size(); i++) {
            plots.add(new Plot(i + 1, centers.get(i)));
            spawnBlockKeys.add(key(centers.get(i)));
        }
    }

    public boolean assign(List<Player> players) {
        if (players.size() > plots.size()) return false;

        List<Plot> pool = new ArrayList<>(plots);
        Collections.shuffle(pool);

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Plot plot = pool.get(i);
            plot.setOwner(p.getUniqueId(), p.getName());
            byPlayer.put(p.getUniqueId(), plot);
        }
        return true;
    }

    public void trackPlace(UUID uuid, Location loc) {
        placedBlocks.computeIfAbsent(uuid, k -> new HashSet<>()).add(key(loc));
    }

    public void untrackPlace(UUID uuid, Location loc) {
        Set<String> placed = placedBlocks.get(uuid);
        if (placed != null) placed.remove(key(loc));
    }

    public boolean playerPlaced(UUID uuid, Location loc) {
        Set<String> placed = placedBlocks.get(uuid);
        return placed != null && placed.contains(key(loc));
    }

    public boolean isSpawnBlock(Location loc) {
        return spawnBlockKeys.contains(key(loc));
    }

    public Material getSpawnBlockMaterial() {
        return spawnBlockMaterial;
    }

    public Plot plotOf(UUID uuid) {
        return byPlayer.get(uuid);
    }

    public List<Plot> assigned() {
        List<Plot> result = new ArrayList<>();
        for (Plot p : plots) {
            if (p.hasOwner()) result.add(p);
        }
        return result;
    }

    public boolean isInsidePlot(Plot plot, Location loc) {
        Location center = plot.getCenter();
        if (loc.getWorld() == null || !loc.getWorld().equals(center.getWorld())) return false;
        int half = plugin.getConfigManager().getPlotSize() / 2;
        return Math.abs(loc.getBlockX() - center.getBlockX()) <= half
            && Math.abs(loc.getBlockZ() - center.getBlockZ()) <= half;
    }

    public boolean isInsideOwnPlot(UUID uuid, Location loc) {
        Plot plot = byPlayer.get(uuid);
        if (plot == null) return false;
        return isInsidePlot(plot, loc);
    }

    public void clearBlocks(Plot plot) {
        Location center = plot.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        int half = plugin.getConfigManager().getPlotSize() / 2;
        int baseY = center.getBlockY();
        for (int x = center.getBlockX() - half; x <= center.getBlockX() + half; x++) {
            for (int z = center.getBlockZ() - half; z <= center.getBlockZ() + half; z++) {
                for (int y = baseY + 1; y <= baseY + 60; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
        if (plot.getOwnerUuid() != null) placedBlocks.remove(plot.getOwnerUuid());
    }

    public void reset() {
        plots.clear();
        byPlayer.clear();
        placedBlocks.clear();
        spawnBlockKeys.clear();
        spawnBlockMaterial = null;
    }

    public int plotCount() { return plots.size(); }

    private String key(Location loc) {
        if (loc.getWorld() == null) return "";
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }
}
