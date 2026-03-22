package me.aroze.simplectf.game;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.team.TeamColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

@Accessors(fluent = true)
public final class CTFGame {

    @Getter
    private static final CTFGame instance = new CTFGame();

    private final Map<TeamColor, Team> teams = new EnumMap<>(TeamColor.class);

    public CTFGame() {
        for (TeamColor teamColor : TeamColor.values()) {
            teams.put(teamColor, new Team());
        }
    }

    @NotNull
    public Team getTeam(final TeamColor teamColor) {
        return teams.get(teamColor);
    }

    @Nullable
    public Team getTeam(final CTFPlayer ctfPlayer) {
        final @Nullable TeamColor color = ctfPlayer.teamColor();
        if (color == null) {
            return null;
        }

        return teams.get(color);
    }

    public void setTeam(final CTFPlayer ctfPlayer, final TeamColor teamColor) {
        final Team currentTeam = getTeam(ctfPlayer);

        if (currentTeam != null) {
            currentTeam.members().remove(ctfPlayer.uuid());
        }

        final Team newTeam = teams.get(teamColor);
        newTeam.members().add(ctfPlayer.uuid());

        ctfPlayer.teamColor(teamColor);
    }
}
