package br.com.m4trixdev.command;

import br.com.m4trixdev.Main;
import br.com.m4trixdev.manager.EventManager;
import br.com.m4trixdev.manager.EventManager.Phase1Result;
import br.com.m4trixdev.model.EventState;
import br.com.m4trixdev.model.Plot;
import br.com.m4trixdev.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BBCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public BBCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("buildbattle.admin")) {
            sender.sendMessage(plugin.getConfigManager().fmt(
                plugin.getConfigManager().getMsgNoPermission()));
            return true;
        }

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "set"    -> doSet(sender, args);
            case "start"  -> doStart(sender, args);
            case "stop"   -> doStop(sender);
            case "info"   -> doInfo(sender);
            case "reload" -> doReload(sender);
            default       -> sendHelp(sender);
        }
        return true;
    }

    private void doSet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.c("&cApenas jogadores podem usar /bb set."));
            return;
        }
        if (args.length < 2) { sendUsage(sender, "/bb set <spawn|spawnblock|area>"); return; }

        switch (args[1].toLowerCase()) {
            case "spawn" -> {
                plugin.getDataManager().setSpawn(player.getLocation());
                player.sendMessage(plugin.getConfigManager().fmt(
                    plugin.getConfigManager().getMsgSpawnSet()));
            }
            case "spawnblock" -> {
                Block target = player.getTargetBlockExact(10);
                if (target == null || target.getType() == Material.AIR) {
                    player.sendMessage(plugin.getConfigManager().fmt(
                        "&cOlhe para um bloco (ate 10 blocos de distancia)."));
                    return;
                }
                Material mat = target.getType();
                plugin.getDataManager().setSpawnBlockMaterial(mat);
                player.sendMessage(plugin.getConfigManager().fmt(
                    "&aMaterial registrado: &f" + mat.name()
                    + "&a. Todos os blocos desse tipo na area serao plots."));
            }
            case "area" -> {
                if (args.length < 3) { sendUsage(sender, "/bb set area <pos1|pos2>"); return; }
                switch (args[2].toLowerCase()) {
                    case "pos1" -> {
                        plugin.getDataManager().setAreaPos1(player.getLocation());
                        player.sendMessage(plugin.getConfigManager().fmt(
                            "&aPos1 da area definida: &f" + fmtLoc(player.getLocation())));
                    }
                    case "pos2" -> {
                        plugin.getDataManager().setAreaPos2(player.getLocation());
                        player.sendMessage(plugin.getConfigManager().fmt(
                            "&aPos2 da area definida: &f" + fmtLoc(player.getLocation())));
                    }
                    default -> sendUsage(sender, "/bb set area <pos1|pos2>");
                }
            }
            default -> sendUsage(sender, "/bb set <spawn|spawnblock|area>");
        }
    }

    private void doStart(CommandSender sender, String[] args) {
        if (args.length < 2) { sendUsage(sender, "/bb start <1|2|3|4> [tema]"); return; }

        int phase;
        try { phase = Integer.parseInt(args[1]); }
        catch (NumberFormatException e) { sendUsage(sender, "/bb start <1|2|3|4>"); return; }

        switch (phase) {
            case 1 -> {
                if (plugin.getEventManager().getState() != EventState.WAITING) {
                    sender.sendMessage(plugin.getConfigManager().fmt("&cEvento ja em andamento. Use /bb stop."));
                    return;
                }
                String chosenTheme = args.length >= 3
                    ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
                var result = plugin.getEventManager().startPhase1(chosenTheme);
                switch (result) {
                    case NO_SPAWN ->
                        sender.sendMessage(plugin.getConfigManager().fmt("&cSpawn nao configurado. Use /bb set spawn."));
                    case NO_MATERIAL ->
                        sender.sendMessage(plugin.getConfigManager().fmt("&cMaterial nao definido. Use /bb set spawnblock."));
                    case NO_AREA ->
                        sender.sendMessage(plugin.getConfigManager().fmt("&cArea nao configurada. Use /bb set area pos1 e pos2."));
                    case NO_PLOTS_FOUND -> {
                        Material dbMat = plugin.getDataManager().getSpawnBlockMaterial();
                        Location dbP1  = plugin.getDataManager().getAreaPos1();
                        Location dbP2  = plugin.getDataManager().getAreaPos2();
                        sender.sendMessage(plugin.getConfigManager().fmt(
                            "&cNenhum bloco de &f" + (dbMat != null ? dbMat.name() : "?")
                            + " &cencontrado na area. Verifique /bb info."));
                        sender.sendMessage(ColorUtil.c("&7Area: &f"
                            + (dbP1 != null ? fmtLoc(dbP1) : "?") + " &7ate &f"
                            + (dbP2 != null ? fmtLoc(dbP2) : "?")));
                    }
                    case NO_PLAYERS ->
                        sender.sendMessage(plugin.getConfigManager().fmt("&cNenhum jogador online."));
                    case NOT_ENOUGH_PLOTS ->
                        sender.sendMessage(plugin.getConfigManager().fmt("&cPlots insuficientes para os jogadores online."));
                    case OK -> {}
                }
            }
            case 2 -> {
                if (plugin.getEventManager().getState() != EventState.PHASE_1) {
                    sender.sendMessage(plugin.getConfigManager().fmt("&cExecute /bb start 1 primeiro."));
                    return;
                }
                plugin.getEventManager().startPhase2();
            }
            case 3 -> {
                EventState st = plugin.getEventManager().getState();
                if (st != EventState.BUILDING && st != EventState.PHASE_1) {
                    sender.sendMessage(plugin.getConfigManager().fmt("&cFase invalida para iniciar a votacao."));
                    return;
                }
                if (!plugin.getEventManager().startPhase3()) {
                    sender.sendMessage(plugin.getConfigManager().fmt("&cNenhum plot atribuido para votar."));
                }
            }
            case 4 -> {
                if (plugin.getEventManager().getState() != EventState.VOTING) {
                    sender.sendMessage(plugin.getConfigManager().fmt("&cExecute /bb start 3 primeiro."));
                    return;
                }
                plugin.getEventManager().startPhase4();
            }
            default -> sendUsage(sender, "/bb start <1|2|3|4>");
        }
    }

    private void doStop(CommandSender sender) {
        if (plugin.getEventManager().getState() == EventState.WAITING) {
            sender.sendMessage(plugin.getConfigManager().fmt("&cNenhum evento em andamento."));
            return;
        }
        plugin.getEventManager().stop();
    }

    private void doInfo(CommandSender sender) {
        EventManager em = plugin.getEventManager();
        Material mat = plugin.getDataManager().getSpawnBlockMaterial();
        Location p1  = plugin.getDataManager().getAreaPos1();
        Location p2  = plugin.getDataManager().getAreaPos2();

        sender.sendMessage(ColorUtil.c("&6----- Build Battle Info -----"));
        sender.sendMessage(ColorUtil.c("&7Estado: &f" + em.getState().name()));
        sender.sendMessage(ColorUtil.c("&7Tema: &f" + (em.getTheme().isEmpty() ? "Nao definido" : em.getTheme())));
        sender.sendMessage(ColorUtil.c("&7Participantes: &f" + em.getParticipants().size()));
        sender.sendMessage(ColorUtil.c("&7Material do plot: &f" + (mat != null ? mat.name() : "nao definido")));
        sender.sendMessage(ColorUtil.c("&7Area pos1: &f" + (p1 != null ? fmtLoc(p1) : "nao definida")));
        sender.sendMessage(ColorUtil.c("&7Area pos2: &f" + (p2 != null ? fmtLoc(p2) : "nao definida")));
        sender.sendMessage(ColorUtil.c("&7Spawn: &f" + (plugin.getDataManager().getSpawn() != null
            ? fmtLoc(plugin.getDataManager().getSpawn()) : "nao definido")));

        if (em.getState() == EventState.BUILDING) {
            int r = em.getBuildTimer();
            sender.sendMessage(ColorUtil.c("&7Tempo restante: &f" + String.format("%02d:%02d", r / 60, r % 60)));
        }

        if (em.getState() == EventState.VOTING) {
            int total = em.getVotingQueue() != null ? em.getVotingQueue().size() : 0;
            sender.sendMessage(ColorUtil.c("&7Votacao: &fPlot " + (em.getVoteIndex() + 1) + "/" + total));
            for (Plot p : plugin.getPlotManager().assigned()) {
                sender.sendMessage(ColorUtil.c("  &7#" + p.getId() + " &f" + p.getOwnerName()
                    + " &7- &e" + plugin.getVoteManager().getScore(p.getId()) + " pts"));
            }
        }
    }

    private void doReload(CommandSender sender) {
        plugin.getConfigManager().load();
        sender.sendMessage(plugin.getConfigManager().fmt("&aConfiguracoes recarregadas."));
    }

    private String fmtLoc(Location loc) {
        if (loc == null) return "?";
        String world = loc.getWorld() != null ? loc.getWorld().getName() : "?";
        return String.format("%s (%.0f, %.0f, %.0f)", world, loc.getX(), loc.getY(), loc.getZ());
    }

    private void sendUsage(CommandSender sender, String usage) {
        sender.sendMessage(plugin.getConfigManager().fmt("&cUso: &f" + usage));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.c("&6----- Build Battle -----"));
        sender.sendMessage(ColorUtil.c("&e/bb set spawn &7- Define o spawn do evento"));
        sender.sendMessage(ColorUtil.c("&e/bb set spawnblock &7- Olhe para 1 bloco, registra o material"));
        sender.sendMessage(ColorUtil.c("&e/bb set area pos1 &7- Define o canto 1 da area de scan"));
        sender.sendMessage(ColorUtil.c("&e/bb set area pos2 &7- Define o canto 2 da area de scan"));
        sender.sendMessage(ColorUtil.c("&e/bb start 1 [tema] &7- Escaneia area e teleporta jogadores"));
        sender.sendMessage(ColorUtil.c("&e/bb start 2 &7- Inicia a construcao"));
        sender.sendMessage(ColorUtil.c("&e/bb start 3 &7- Inicia a votacao (8s por plot)"));
        sender.sendMessage(ColorUtil.c("&e/bb start 4 &7- Finaliza e anuncia top 3"));
        sender.sendMessage(ColorUtil.c("&e/bb stop &7- Para o evento imediatamente"));
        sender.sendMessage(ColorUtil.c("&e/bb info &7- Informacoes do evento"));
        sender.sendMessage(ColorUtil.c("&e/bb reload &7- Recarrega config.yml"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("buildbattle.admin")) return Collections.emptyList();
        if (args.length == 1) return List.of("set", "start", "stop", "info", "reload");
        if (args.length == 2) return switch (args[0].toLowerCase()) {
            case "set"   -> List.of("spawn", "spawnblock", "area");
            case "start" -> List.of("1", "2", "3", "4");
            default      -> Collections.emptyList();
        };
        if (args.length == 3 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("area")) {
            return List.of("pos1", "pos2");
        }
        return Collections.emptyList();
    }
}
