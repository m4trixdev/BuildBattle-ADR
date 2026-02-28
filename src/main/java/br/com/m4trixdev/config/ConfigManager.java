package br.com.m4trixdev.config;

import br.com.m4trixdev.Main;
import br.com.m4trixdev.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final Main plugin;

    private int buildTime;
    private int voteTime;
    private int plotSize;
    private boolean blockTnt;
    private boolean blockLava;
    private boolean blockWater;
    private boolean scoreboardEnabled;
    private String scoreboardTitle;
    private List<String> scoreboardLines;
    private String prefix;
    private String msgPhase1Start;
    private String msgPhase2Start;
    private String msgPhase2Title;
    private String msgPhase2Subtitle;
    private String msgPhase3Start;
    private String msgNextPlot;
    private String msgVoteOk;
    private String msgAlreadyVoted;
    private String msgOwnPlot;
    private String msgNoPermission;
    private String msgDeleted;
    private String msgVotingDone;
    private String msgPhase4Start;
    private String msgTop1;
    private String msgTop2;
    private String msgTop3;
    private String msgSpawnSet;
    private String msgStopped;
    private String msgNoSpawn;
    private String msgNoPlots;
    private String msgOutside;
    private String msgTimeUp;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean load() {
        try {
            FileConfiguration c = loadUtf8Config();
            buildTime         = c.getInt("event.build-time",  1800);
            voteTime          = c.getInt("event.vote-time",      8);
            plotSize          = c.getInt("event.plot-size",     25);
            blockTnt          = c.getBoolean("protection.block-tnt",   true);
            blockLava         = c.getBoolean("protection.block-lava",  true);
            blockWater        = c.getBoolean("protection.block-water", true);
            scoreboardEnabled = c.getBoolean("scoreboard.enabled", true);
            scoreboardTitle   = str(c, "scoreboard.title", "&6Build Battle");
            scoreboardLines   = c.getStringList("scoreboard.lines");
            prefix            = str(c, "messages.prefix",          "&6[BB] &r");
            msgPhase1Start    = str(c, "messages.phase1-start",    "&aJogadores teleportados para os plots!");
            msgPhase2Start    = str(c, "messages.phase2-start",    "&aConstrução iniciada! Tema: &f%theme%");
            msgPhase2Title    = str(c, "messages.phase2-title",    "&a&lConstrução Iniciada!");
            msgPhase2Subtitle = str(c, "messages.phase2-subtitle", "&7Tema: &f%theme%");
            msgPhase3Start    = str(c, "messages.phase3-start",    "&eVotação iniciada!");
            msgNextPlot       = str(c, "messages.next-plot",       "&eAvaliando (%i%/%total%): &f%player%");
            msgVoteOk         = str(c, "messages.vote-ok",         "&aVoto registrado: &f%score% pontos");
            msgAlreadyVoted   = str(c, "messages.already-voted",   "&cVocê já votou neste plot.");
            msgOwnPlot        = str(c, "messages.own-plot",        "&cVocê não pode votar no próprio plot.");
            msgNoPermission   = str(c, "messages.no-permission",   "&cSem permissão.");
            msgDeleted        = str(c, "messages.deleted",         "&cConstrução de &f%player% &cdeletada.");
            msgVotingDone     = str(c, "messages.voting-done",     "&aVotação encerrada! Use /bb start 4 para anunciar o resultado.");
            msgPhase4Start    = str(c, "messages.phase4-start",    "&6&lResultado Final:");
            msgTop1           = str(c, "messages.top1",            "&6#1 &f%player% &7- &e%pts% pts");
            msgTop2           = str(c, "messages.top2",            "&7#2 &f%player% &7- &e%pts% pts");
            msgTop3           = str(c, "messages.top3",            "&c#3 &f%player% &7- &e%pts% pts");
            msgSpawnSet       = str(c, "messages.spawn-set",       "&aSpawn salvo.");
            msgStopped        = str(c, "messages.stopped",         "&cEvento encerrado.");
            msgNoSpawn        = str(c, "messages.no-spawn",        "&cSpawn não configurado. Use /bb set spawn.");
            msgNoPlots        = str(c, "messages.no-plots",        "&cNenhum plot encontrado na área. Verifique o material e a área definida.");
            msgOutside        = str(c, "messages.outside",         "&cFique dentro do seu plot!");
            msgTimeUp         = str(c, "messages.time-up",         "&c&lTempo esgotado!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar config.yml: " + e.getMessage());
            return false;
        }
    }

    private FileConfiguration loadUtf8Config() throws IOException {
        File dataFolder = plugin.getDataFolder();
        File configFile = new File(dataFolder, "config.yml");

        if (!configFile.exists()) {
            dataFolder.mkdirs();
            try (InputStream in = plugin.getResource("config.yml")) {
                if (in != null) Files.copy(in, configFile.toPath());
            }
        }

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        }
    }

    private String str(FileConfiguration c, String path, String def) {
        String v = c.getString(path, def);
        return (v == null || v.isEmpty()) ? def : v;
    }

    public String fmt(String msg) {
        return ColorUtil.c(prefix + msg);
    }

    public int getBuildTime()             { return buildTime; }
    public int getVoteTime()              { return voteTime; }
    public int getPlotSize()              { return plotSize; }
    public boolean isBlockTnt()           { return blockTnt; }
    public boolean isBlockLava()          { return blockLava; }
    public boolean isBlockWater()         { return blockWater; }
    public boolean isScoreboardEnabled()  { return scoreboardEnabled; }
    public String getScoreboardTitle()    { return scoreboardTitle; }
    public List<String> getScoreboardLines() {
        return scoreboardLines.isEmpty() ? new ArrayList<>() : scoreboardLines;
    }
    public String getMsgPhase1Start()     { return msgPhase1Start; }
    public String getMsgPhase2Start()     { return msgPhase2Start; }
    public String getMsgPhase2Title()     { return msgPhase2Title; }
    public String getMsgPhase2Subtitle()  { return msgPhase2Subtitle; }
    public String getMsgPhase3Start()     { return msgPhase3Start; }
    public String getMsgNextPlot()        { return msgNextPlot; }
    public String getMsgVoteOk()          { return msgVoteOk; }
    public String getMsgAlreadyVoted()    { return msgAlreadyVoted; }
    public String getMsgOwnPlot()         { return msgOwnPlot; }
    public String getMsgNoPermission()    { return msgNoPermission; }
    public String getMsgDeleted()         { return msgDeleted; }
    public String getMsgVotingDone()      { return msgVotingDone; }
    public String getMsgPhase4Start()     { return msgPhase4Start; }
    public String getMsgTop1()            { return msgTop1; }
    public String getMsgTop2()            { return msgTop2; }
    public String getMsgTop3()            { return msgTop3; }
    public String getMsgSpawnSet()        { return msgSpawnSet; }
    public String getMsgStopped()         { return msgStopped; }
    public String getMsgNoSpawn()         { return msgNoSpawn; }
    public String getMsgNoPlots()         { return msgNoPlots; }
    public String getMsgOutside()         { return msgOutside; }
    public String getMsgTimeUp()          { return msgTimeUp; }
}
