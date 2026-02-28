package br.com.m4trixdev.manager;

import br.com.m4trixdev.model.Plot;

import java.util.*;

public class VoteManager {

    private final Map<Integer, Integer> scores = new HashMap<>();
    private final Map<UUID, Set<Integer>> history = new HashMap<>();

    public boolean vote(UUID voter, Plot plot, int score) {
        Set<Integer> seen = history.computeIfAbsent(voter, k -> new HashSet<>());
        if (!seen.add(plot.getId())) return false;
        scores.merge(plot.getId(), score, Integer::sum);
        return true;
    }

    public int getScore(int plotId) {
        return scores.getOrDefault(plotId, 0);
    }

    public void reset() {
        scores.clear();
        history.clear();
    }
}
