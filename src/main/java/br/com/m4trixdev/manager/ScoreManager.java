package br.com.m4trixdev.manager;

import br.com.m4trixdev.model.Plot;

import java.util.ArrayList;
import java.util.List;

public class ScoreManager {

    public record Entry(String name, int score) {}

    public List<Entry> rank(List<Plot> assignedPlots, VoteManager votes) {
        List<Entry> list = new ArrayList<>();
        for (Plot p : assignedPlots) {
            list.add(new Entry(p.getOwnerName(), votes.getScore(p.getId())));
        }
        list.sort((a, b) -> Integer.compare(b.score(), a.score()));
        return list;
    }
}
