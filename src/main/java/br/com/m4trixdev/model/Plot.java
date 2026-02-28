package br.com.m4trixdev.model;

import org.bukkit.Location;

import java.util.UUID;

public class Plot {

    private final int id;
    private final Location center;
    private UUID ownerUuid;
    private String ownerName;

    public Plot(int id, Location center) {
        this.id = id;
        this.center = center.clone();
    }

    public void setOwner(UUID uuid, String name) {
        this.ownerUuid = uuid;
        this.ownerName = name;
    }

    public int getId() {
        return id;
    }

    public Location getCenter() {
        return center.clone();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName != null ? ownerName : "?";
    }

    public boolean hasOwner() {
        return ownerUuid != null;
    }

    public boolean isOwner(UUID uuid) {
        return ownerUuid != null && ownerUuid.equals(uuid);
    }
}
