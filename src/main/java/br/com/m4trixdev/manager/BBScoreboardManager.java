package br.com.m4trixdev.manager;

import br.com.m4trixdev.Main;
import br.com.m4trixdev.model.EventState;
import br.com.m4trixdev.model.Plot;
import br.com.m4trixdev.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BBScoreboardManager {

    private final Main plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();
    private int taskId = -1;

    public BBScoreboardManager(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfigManager().isScoreboardEnabled()) return;
        for (Player p : Bukkit.getOnlinePlayers()) setup(p);
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L).getTaskId();
    }

    public void stop() {
        if (taskId != -1) { Bukkit.getScheduler().cancelTask(taskId); taskId = -1; }
        for (Player p : Bukkit.getOnlinePlayers()) remove(p);
        boards.clear();
    }

    public void onJoin(Player p) {
        if (plugin.getConfigManager().isScoreboardEnabled()) setup(p);
    }

    public void onQuit(Player p) {
        boards.remove(p.getUniqueId());
    }

    private void setup(Player player) {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        Scoreboard board = sm.getNewScoreboard();

        Objective obj = board.registerNewObjective("bb", Criteria.DUMMY,
            ColorUtil.c(plugin.getConfigManager().getScoreboardTitle()));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = plugin.getConfigManager().getScoreboardLines();
        int size = Math.min(lines.size(), 15);
        for (int i = 0; i < size; i++) {
            String entry = uniqueEntry(i);
            Team team = board.registerNewTeam("l" + i);
            team.addEntry(entry);
            obj.getScore(entry).setScore(size - i);
        }

        boards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        update(player, board);
    }

    private void remove(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard board = boards.get(p.getUniqueId());
            if (board == null) { setup(p); continue; }
            update(p, board);
        }
    }

    private void update(Player player, Scoreboard board) {
        Objective obj = board.getObjective("bb");
        if (obj == null) return;

        List<String> lines = plugin.getConfigManager().getScoreboardLines();
        int size = Math.min(lines.size(), 15);
        for (int i = 0; i < size; i++) {
            Team team = board.getTeam("l" + i);
            if (team == null) continue;
            team.setPrefix(ColorUtil.c(resolve(lines.get(i), player)));
        }
    }

    private String resolve(String line, Player player) {
        EventManager em = plugin.getEventManager();

        String theme = em.getTheme().isEmpty() ? "Nao definido" : em.getTheme();

        String state = switch (em.getState()) {
            case WAITING  -> "&7Aguardando";
            case PHASE_1  -> "&ePreparo";
            case BUILDING -> "&aConstruindo";
            case VOTING   -> "&bVotacao";
            case ENDED    -> "&cEncerrado";
        };

        String time;
        if (em.getState() == EventState.BUILDING) {
            int r = em.getBuildTimer();
            time = String.format("%02d:%02d", r / 60, r % 60);
        } else if (em.getState() == EventState.VOTING) {
            time = em.getVoteTimer() + "s";
        } else {
            time = "--:--";
        }

        String plotId = "---";
        Plot plot = plugin.getPlotManager().plotOf(player.getUniqueId());
        if (plot != null) plotId = String.valueOf(plot.getId());

        String builder = "---";
        if (em.getState() == EventState.VOTING) {
            Plot vp = em.currentVotingPlot();
            if (vp != null) builder = vp.getOwnerName();
        }

        return line
            .replace("%player%",  player.getName())
            .replace("%state%",   state)
            .replace("%time%",    time)
            .replace("%plot%",    plotId)
            .replace("%theme%",   theme)
            .replace("%builder%", builder);
    }

    private String uniqueEntry(int i) {
        return ChatColor.values()[i % ChatColor.values().length].toString() + ChatColor.RESET;
    }
}
