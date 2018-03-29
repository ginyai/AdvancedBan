package me.leoko.advancedban.sponge.event;

import me.leoko.advancedban.utils.Punishment;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class RevokePunishmentEvent extends AbstractEvent {

    private final Punishment punishment;
    private final boolean massClear;
    private final Cause cause;

    public RevokePunishmentEvent(Punishment punishment, boolean massClear, Cause cause) {
        this.punishment = punishment;
        this.massClear = massClear;
        this.cause = cause;
    }

    /**
     * Returns the punishment involved in this event
     *
     * @return Punishment
     */
    public Punishment getPunishment() {
        return punishment;
    }

    /**
     * If this event is part of a mass clearing of punishments
     * <p>Useful to reduce spam/noise</p>
     *
     * @return True if part of a mass clearing
     */
    public boolean isMassClear() {
        return massClear;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
