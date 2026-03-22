package me.aroze.simplectf.team;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.game.CTFGame;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents data for a team in a {@link CTFGame}
 */
@Getter
@Accessors(fluent = true)
public class Team {

    /** The UUIDs of all members in this team */
    private final List<UUID> members = new ArrayList<>();

}

