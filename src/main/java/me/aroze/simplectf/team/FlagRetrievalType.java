package me.aroze.simplectf.team;

/**
 * Represents how the flag was retrieved to base.
 */
public enum FlagRetrievalType {
    /** The flag was retrieved due to the game resetting */
    RESET,

    /** The flag was captured by an opposing team */
    CAPTURED,

    /** The flag was returned to base by a team member */
    RETURNED,
}
