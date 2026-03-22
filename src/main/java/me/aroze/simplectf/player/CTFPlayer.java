package me.aroze.simplectf.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.team.TeamColor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class CTFPlayer {

    private final UUID uuid;
    private @Nullable TeamColor teamColor = null;

}
