package me.leoko.advancedban.sponge;

import com.google.inject.Inject;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.sponge.listener.ChatListenerSponge;
import me.leoko.advancedban.sponge.listener.CommandListenerSponge;
import me.leoko.advancedban.sponge.listener.ConnectionListenerSponge;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

@Plugin(
        //todo:
        id = "advancedban",
        name = "AdvancedBan",
        version = "2.1.4",
        authors = "Leoko"
)
public class SpongeMain {
    private static SpongeMain instance;

    public static SpongeMain get(){
        return instance;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    Path configDir;

    @Inject
    Logger logger;

    @Inject
    PluginContainer container;

    @Inject
    Metrics metrics;


    @Listener
    public void onGamePreInit(GamePreInitializationEvent event){
        instance = this;
    }

    @Listener
    public void onStartingServer(GameStartingServerEvent event){
        Universal.get().setup(new SpongeMethods(this));
        Sponge.getEventManager().registerListeners(this,new ChatListenerSponge());
        Sponge.getEventManager().registerListeners(this,new CommandListenerSponge());
        Sponge.getEventManager().registerListeners(this,new ConnectionListenerSponge());
    }

    @Listener
    public void onStopedServer(GameStoppedServerEvent event){
        Universal.get().shutdown();
    }
}
