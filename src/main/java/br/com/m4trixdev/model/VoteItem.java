package br.com.m4trixdev.model;

import org.bukkit.Material;

public enum VoteItem {

    SCORE_1(Material.RED_WOOL,    1, "&cNota 1"),
    SCORE_2(Material.ORANGE_WOOL, 2, "&6Nota 2"),
    SCORE_3(Material.YELLOW_WOOL, 3, "&eNota 3"),
    SCORE_4(Material.LIME_WOOL,   4, "&aNota 4"),
    SCORE_5(Material.DIAMOND,     5, "&bNota 5 (Max)"),
    DELETE (Material.BARRIER,    -1, "&cDeletar construcao");

    private final Material material;
    private final int score;
    private final String displayName;

    VoteItem(Material material, int score, String displayName) {
        this.material = material;
        this.score = score;
        this.displayName = displayName;
    }

    public Material getMaterial() { return material; }
    public int getScore()         { return score; }
    public String getDisplayName(){ return displayName; }

    public static VoteItem fromMaterial(Material mat) {
        for (VoteItem v : values()) {
            if (v.material == mat) return v;
        }
        return null;
    }
}
