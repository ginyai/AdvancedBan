package me.leoko.advancedban.sponge.listener;


import me.leoko.advancedban.Universal;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

public class ChatListenerSponge {
    @Listener(order = Order.FIRST,beforeModifications = true)
    public void onChat(MessageChannelEvent.Chat event, @Root Player player){
        if(Universal.get().getMethods().callChat(player)){
            event.setCancelled(true);
        }
    }
}
