package br.com.m4trixdev.manager;

import br.com.m4trixdev.Main;
import br.com.m4trixdev.model.EventState;
import br.com.m4trixdev.model.Plot;
import br.com.m4trixdev.model.VoteItem;
import br.com.m4trixdev.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class EventManager {

    private final Main plugin;

    private EventState state = EventState.WAITING;
    private String theme = "";
    private int buildTimer = 0;
    private int voteTimer  = 0;

    private BukkitTask buildTask;
    private BukkitTask voteTask;

    private final List<UUID> participants = new ArrayList<>();
    private List<Plot> votingQueue;
    private int voteIndex = 0;

    public EventManager(Main plugin) {
        this.plugin = plugin;
    }

    public enum Phase1Result {
        OK,
        NO_SPAWN,
        NO_MATERIAL,
        NO_AREA,
        NO_PLOTS_FOUND,
        NO_PLAYERS,
        NOT_ENOUGH_PLOTS
    }

    public Phase1Result startPhase1(String chosenTheme) {
        if (state != EventState.WAITING) return Phase1Result.NO_SPAWN; // guarded by command

        if (plugin.getDataManager().getSpawn() == null) return Phase1Result.NO_SPAWN;

        Material mat = plugin.getDataManager().getSpawnBlockMaterial();
        if (mat == null) return Phase1Result.NO_MATERIAL;

        if (!plugin.getDataManager().isAreaConfigured()) return Phase1Result.NO_AREA;

        Location pos1 = plugin.getDataManager().getAreaPos1();
        Location pos2 = plugin.getDataManager().getAreaPos2();

        List<Location> centers = plugin.getPlotManager().scanForPlots(pos1, pos2, mat);
        if (centers.isEmpty()) return Phase1Result.NO_PLOTS_FOUND;

        List<Player> contestants = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("buildbattle.bypass")) contestants.add(p);
        }
        if (contestants.isEmpty()) return Phase1Result.NO_PLAYERS;
        if (centers.size() < contestants.size()) return Phase1Result.NOT_ENOUGH_PLOTS;

        plugin.getPlotManager().buildPlots(centers, mat);
        plugin.getPlotManager().assign(contestants);

        participants.clear();
        for (Player p : contestants) participants.add(p.getUniqueId());

        theme = (chosenTheme != null && !chosenTheme.isBlank()) ? chosenTheme : "";
        state = EventState.PHASE_1;

        for (Player p : contestants) {
            Plot plot = plugin.getPlotManager().plotOf(p.getUniqueId());
            if (plot == null) continue;
            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().clear();
            p.teleport(spawnOnPlot(plot.getCenter()));
        }

        broadcast(plugin.getConfigManager().getMsgPhase1Start());
        return Phase1Result.OK;
    }

    public boolean startPhase2() {
        if (state != EventState.PHASE_1) return false;

        state = EventState.BUILDING;
        buildTimer = plugin.getConfigManager().getBuildTime();

        String display = theme.isEmpty() ? "Nao definido" : theme;

        for (UUID id : participants) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) continue;
            p.setGameMode(GameMode.CREATIVE);
            p.sendTitle(
                ColorUtil.c(plugin.getConfigManager().getMsgPhase2Title()),
                ColorUtil.c(plugin.getConfigManager().getMsgPhase2Subtitle().replace("%theme%", display)),
                10, 70, 20
            );
        }

        broadcast(plugin.getConfigManager().getMsgPhase2Start().replace("%theme%", display));

        buildTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            buildTimer--;
            if (buildTimer == 60) broadcast("&e60 segundos restantes!");
            if (buildTimer == 30) broadcast("&e30 segundos restantes!");
            if (buildTimer == 10) broadcast("&c10 segundos restantes!");
            if (buildTimer <= 0) {
                buildTask.cancel();
                buildTask = null;
                broadcast(plugin.getConfigManager().getMsgTimeUp());
            }
        }, 20L, 20L);

        return true;
    }

    public boolean startPhase3() {
        if (state != EventState.BUILDING && state != EventState.PHASE_1) return false;

        cancelBuildTask();
        state = EventState.VOTING;
        plugin.getVoteManager().reset();

        votingQueue = new ArrayList<>(plugin.getPlotManager().assigned());
        voteIndex = 0;

        if (votingQueue.isEmpty()) return false;

        broadcast(plugin.getConfigManager().getMsgPhase3Start());

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            if (p.hasPermission("buildbattle.admin")) {
                p.setGameMode(GameMode.CREATIVE);
                giveVoteItems(p);
            } else {
                p.setGameMode(GameMode.ADVENTURE);
            }
        }

        teleportToCurrentPlot();
        startVoteTask();
        return true;
    }

    private void startVoteTask() {
        voteTimer = plugin.getConfigManager().getVoteTime();

        voteTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            voteTimer--;
            sendVoteActionBar();
            if (voteTimer > 0) return;

            voteTask.cancel();
            voteTask = null;

            voteIndex++;
            if (voteIndex >= votingQueue.size()) {
                broadcast(plugin.getConfigManager().getMsgVotingDone());
                return;
            }

            teleportToCurrentPlot();
            startVoteTask();
        }, 20L, 20L);
    }

    private void sendVoteActionBar() {
        if (votingQueue == null || voteIndex >= votingQueue.size()) return;
        String raw = "&eAvaliando: &f" + votingQueue.get(voteIndex).getOwnerName()
            + "  &7| &eTempo: &f" + voteTimer + "s"
            + "  &7| &e" + (voteIndex + 1) + "/" + votingQueue.size();
        Component bar = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendActionBar(bar);
        }
    }

    private void teleportToCurrentPlot() {
        if (votingQueue == null || voteIndex >= votingQueue.size()) return;
        Plot plot = votingQueue.get(voteIndex);

        broadcast(plugin.getConfigManager().getMsgNextPlot()
            .replace("%i%", String.valueOf(voteIndex + 1))
            .replace("%total%", String.valueOf(votingQueue.size()))
            .replace("%player%", plot.getOwnerName()));

        Location dest = spawnOnPlot(plot.getCenter());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(dest);
        }
    }

    private void giveVoteItems(Player p) {
        if (!p.hasPermission("buildbattle.admin")) return;
        p.getInventory().clear();
        int slot = 0;
        for (VoteItem vi : VoteItem.values()) {
            p.getInventory().setItem(slot++, makeVoteItem(vi));
        }
    }

    private ItemStack makeVoteItem(VoteItem vi) {
        ItemStack item = new ItemStack(vi.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.c(vi.getDisplayName()));
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean startPhase4() {
        if (state != EventState.VOTING) return false;

        cancelVoteTask();
        state = EventState.ENDED;

        List<ScoreManager.Entry> ranking = plugin.getScoreManager()
            .rank(plugin.getPlotManager().assigned(), plugin.getVoteManager());

        broadcast(plugin.getConfigManager().getMsgPhase4Start());
        announceRanking(ranking);

        Location spawn = plugin.getDataManager().getSpawn();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            if (spawn != null) p.teleport(spawn);
        }

        reset();
        return true;
    }

    private void announceRanking(List<ScoreManager.Entry> ranking) {
        String[] templates = {
            plugin.getConfigManager().getMsgTop1(),
            plugin.getConfigManager().getMsgTop2(),
            plugin.getConfigManager().getMsgTop3()
        };
        for (int i = 0; i < Math.min(ranking.size(), 3); i++) {
            ScoreManager.Entry e = ranking.get(i);
            Bukkit.broadcastMessage(ColorUtil.c(templates[i]
                .replace("%player%", e.name())
                .replace("%pts%", String.valueOf(e.score()))));
        }
    }

    public void stop() {
        cancelBuildTask();
        cancelVoteTask();

        Location spawn = plugin.getDataManager().getSpawn();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            if (spawn != null) p.teleport(spawn);
        }

        broadcast(plugin.getConfigManager().getMsgStopped());
        reset();
    }

    private void cancelBuildTask() {
        if (buildTask != null) { buildTask.cancel(); buildTask = null; }
    }

    private void cancelVoteTask() {
        if (voteTask != null) { voteTask.cancel(); voteTask = null; }
    }

    private void reset() {
        state = EventState.WAITING;
        theme = "";
        buildTimer = 0;
        voteTimer  = 0;
        voteIndex  = 0;
        votingQueue = null;
        participants.clear();
        plugin.getPlotManager().reset();
        plugin.getVoteManager().reset();
    }

    public Location spawnOnPlot(Location center) {
        return new Location(center.getWorld(),
            center.getBlockX() + 0.5,
            center.getBlockY() + 1.0,
            center.getBlockZ() + 0.5,
            center.getYaw(), 0f);
    }

    private void broadcast(String msg) {
        if (msg == null || msg.isEmpty()) return;
        Bukkit.broadcastMessage(plugin.getConfigManager().fmt(msg));
    }

    public void removeParticipant(UUID uuid) {
        participants.remove(uuid);
    }

    public Plot currentVotingPlot() {
        if (votingQueue == null || voteIndex >= votingQueue.size()) return null;
        return votingQueue.get(voteIndex);
    }

    public EventState getState()      { return state; }
    public String getTheme()          { return theme; }
    public int getBuildTimer()        { return buildTimer; }
    public int getVoteTimer()         { return voteTimer; }
    public int getVoteIndex()         { return voteIndex; }
    public List<Plot> getVotingQueue(){ return votingQueue; }
    public List<UUID> getParticipants(){ return Collections.unmodifiableList(participants); }
}
