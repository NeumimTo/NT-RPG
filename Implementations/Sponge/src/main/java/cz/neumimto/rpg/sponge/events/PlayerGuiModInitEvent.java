package cz.neumimto.rpg.sponge.events;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;

import java.util.UUID;

/**
 * Created by NeumimTo on 30.1.2016.
 */
public class PlayerGuiModInitEvent implements Event {

    private UUID uuid;

    public PlayerGuiModInitEvent(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Cause getCause() {
        return Cause.of(EventContext.empty(), uuid);
    }

}
