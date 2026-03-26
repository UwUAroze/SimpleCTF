# SimpleCTF

A lightweight Capture The Flag minigame plugin for Paper 1.21.11

# Installation
- Download or build the plugin jar.
- Place the jar in your server's `plugins` directory.
- Restart the server to load the plugin.

## Quick Start

### 1) Configure team flag/base locations

Requires permission `simplectf.setflag`.
```text
/ctf setflag red
/ctf setflag blue
```

### 2) Players join teams

```text
/ctf join red
/ctf join blue
```

### 3) Start the match

Requires permission `simplectf.start`.
```text
/ctf start
```

## Gameplay Loop

- Two teams are available: **Red** and **Blue**.
- Players join a team with `/ctf join <team>`.
- The match timer is 10 minutes.
- During the match:
  - Hit the enemy flag to pick it up.
  - Return to your own base while carrying an enemy flag to capture it, scoring 1 point.
  - If a player carrying a flag is killed or leaves the match, the flag is dropped at their position.
  - Hit your dropped flag to return it to your base.
- A team wins by reaching **3 points** first, or by having the most points when the timer runs out.
- If both teams have the same score when the game ends, the match is a draw.

## Commands

Main command: `/ctf`

Aliases:

- `/simplectf`
- `/capturetheflag`

| Command                    | Description                                              | Requirement                           |
|----------------------------|----------------------------------------------------------|---------------------------------------|
| `/ctf`                     | Show command help                                        | None                                  |
| `/ctf join <red\|blue>`    | Join or switch teams                                     | Players only                          |
| `/ctf leave`               | Leave the current game/team                              | Players only                          |
| `/ctf score`               | Show live score (only while in progress)                 | None                                  |
| `/ctf setflag <red\|blue>` | Set a team's base/flag location to your current position | Players with permission `ctf.setflag` |
| `/ctf start`               | Start a game                                             | Has permission `ctf.start`            |
| `/ctf stop`                | Stop the current game                                    | Has permission `ctf.stop`             |

## Build

```
./gradlew build
```

The plugin JAR will be located in `build/libs/`.
