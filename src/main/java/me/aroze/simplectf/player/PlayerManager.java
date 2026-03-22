package me.aroze.simplectf.player;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerManager {

    @Getter
    private static final PlayerManager instance = new PlayerManager();

    private final Map<UUID, CTFPlayer> players = new HashMap<>();

    public CTFPlayer getPlayer(UUID uuid) {
        return players.computeIfAbsent(uuid, CTFPlayer::new);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

}
