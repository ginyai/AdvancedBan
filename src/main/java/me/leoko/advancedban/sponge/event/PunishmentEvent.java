package me.leoko.advancedban.sponge.event;


import me.leoko.advancedban.utils.Punishment;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class PunishmentEvent extends AbstractEvent{
    private final Punishment punishment;
    private final Cause cause;

    public PunishmentEvent(Punishment punishment, Cause cause) {
        this.punishment = punishment;
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
    //todo:
    @Override
    public Cause getCause() {
        return cause;
    }
}
