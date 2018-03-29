package me.leoko.advancedban.sponge.listener;

import me.leoko.advancedban.Universal;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class CommandListenerSponge {
    @Listener(order = Order.FIRST,beforeModifications = true)
    public void onCommand(SendCommandEvent event, @Root Player player){
        if (Universal.get().getMethods().callCMD(player,event.getCommand())) {
            event.setCancelled(true);
        }
    }
}
