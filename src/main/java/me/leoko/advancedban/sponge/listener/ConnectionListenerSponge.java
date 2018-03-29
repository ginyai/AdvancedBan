package me.leoko.advancedban.sponge.listener;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class ConnectionListenerSponge {
    //todo disallow connect with BanService

    @Listener
    public void onAuth(ClientConnectionEvent.Auth event){
        String result = Universal.get().callConnection(event.getProfile().getName().get(), event.getConnection().getAddress().getAddress().getHostAddress());
        if (result != null) {
            event.setMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(result));
            event.setCancelled(true);
        }
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event){
        //todo:welcome Leoko
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event){
        PunishmentManager.get().discard(event.getTargetEntity().getName());
    }
}
