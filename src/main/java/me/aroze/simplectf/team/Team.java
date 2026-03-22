package me.aroze.simplectf.team;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class Team {

    private final List<UUID> members = new ArrayList<>();

}
