package me.leoko.advancedban.sponge;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.sponge.event.PunishmentEvent;
import me.leoko.advancedban.sponge.event.RevokePunishmentEvent;
import me.leoko.advancedban.sponge.listener.CommandReceiverSponge;
import me.leoko.advancedban.utils.Punishment;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Identifiable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongeMethods implements MethodInterface {
    private SpongeMain plugin;

    private Path configPath;
    private Path messagePath;
    private Path layoutPath;

    private ConfigurationLoader<ConfigurationNode> configLoader;
    private ConfigurationLoader<ConfigurationNode> messageLoader;
    private ConfigurationLoader<ConfigurationNode> layoutLoader;
    private ConfigurationLoader<ConfigurationNode> mysqlLoader;

    private ConfigurationNode configNode;
    private ConfigurationNode messageNode;
    private ConfigurationNode layoutNode;
    private ConfigurationNode mysqlNode;


    public SpongeMethods(SpongeMain plugin) {
        this.plugin = plugin;
        configPath = plugin.configDir.resolve("config.yml");
        messagePath = plugin.configDir.resolve("Messages.yml");
        layoutPath = plugin.configDir.resolve("Layouts.yml");
    }


    @Override
    public void loadFiles() {
        try {
            if(!getDataFolder().exists()){
                getDataFolder().mkdirs();
            }
            //Although it is batter to use Asset API.
            if(!configPath.toFile().exists()){
                Files.copy(SpongeMain.class.getResourceAsStream("/config.yml"),configPath);
            }
            if(!messagePath.toFile().exists()){
                Files.copy(SpongeMain.class.getResourceAsStream("/Messages.yml"),messagePath);
            }
            if(!layoutPath.toFile().exists()){
                Files.copy(SpongeMain.class.getResourceAsStream("/Layouts.yml"),layoutPath);
            }

            configLoader = YAMLConfigurationLoader.builder().setPath(configPath).build();
            messageLoader = YAMLConfigurationLoader.builder().setPath(messagePath).build();
            layoutLoader = YAMLConfigurationLoader.builder().setPath(layoutPath).build();

            configNode = configLoader.load();
            messageNode = messageLoader.load();
            layoutNode = layoutLoader.load();
        } catch (IOException e) {
            plugin.logger.error("Failed to load configs.",e);
        }
    }

    @Override
    public String getFromUrlJson(String url, String key) {
        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonObject json = jp.parse(new InputStreamReader(request.getInputStream())).getAsJsonObject();

            String[] keys = key.split("\\|");
            for (int i = 0; i < keys.length - 1; i++) {
                json = json.get(keys[i]).getAsJsonObject();
            }

            return json.get(keys[keys.length - 1]).toString();
        } catch (Exception exc) {
            return null;
        }
    }

    @Override
    public String getVersion() {
        return plugin.container.getVersion().orElse("");
    }

    @Override
    public String[] getKeys(Object file, String path) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getChildrenMap().keySet().stream()
                .map(Object::toString).collect(Collectors.toList()).toArray(new String[]{});
    }

    @Override
    public Object getConfig() {
        return configNode;
    }

    @Override
    public Object getMessages() {
        return messageNode;
    }

    @Override
    public Object getLayouts() {
        return layoutNode;
    }

    @Override
    public void setupMetrics() {
        plugin.metrics.addCustomChart(new Metrics.SimplePie("MySQL", () -> DatabaseManager.get().isUseMySQL() ? "yes" : "no"));
    }

    @Override
    public Object getPlugin() {
        return plugin;
    }

    @Override
    public File getDataFolder() {
        return plugin.configDir.toFile();
    }

    @Override
    public void setCommandExecutor(String cmd) {
        //todo:replace minecraft commands
        Sponge.getCommandManager().register(
                plugin,
                CommandSpec.builder().
                        executor(new CommandReceiverSponge(cmd)).
                        arguments(CommandReceiverSponge.args).build(),
                cmd
        );
    }

    @Override
    public void sendMessage(Object player, String msg) {
        ((MessageReceiver)player).sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(msg));
    }

    @Override
    public String getName(Object player) {
        return ((CommandSource)player).getName();
    }

    @Override
    public String getName(String uuid) {
        UserStorageService userStorageService = Sponge.getServiceManager().provide(UserStorageService.class).get();
        Optional<User> optionalUser = userStorageService.get(UUID.fromString(uuid));
        return optionalUser.map(User::getName).orElse(null);
    }

    @Override
    public String getIP(Object player) {
        return ((Player)player).getConnection().getAddress().getHostName();
    }

    @Override
    public String getInternUUID(Object player) {
        return player instanceof Identifiable ? ((Identifiable)player).getUniqueId().toString().replaceAll("-",""):"none";
    }

    @Override
    public String getInternUUID(String player) {
        UserStorageService userStorageService = Sponge.getServiceManager().provide(UserStorageService.class).get();
        Optional<User> optionalUser = userStorageService.get(player);
        if(optionalUser.isPresent()){
            return optionalUser.get().getUniqueId().toString().replaceAll("-","");
        }else {
            //null? none?
            return null;
        }
    }

    @Override
    public boolean hasPerms(Object player, String perms) {
        return ((Subject)player).hasPermission(perms);
    }

    @Override
    public boolean isOnline(String name) {
        return Sponge.getServer().getPlayer(name).isPresent();
    }

    @Override
    public Object getPlayer(String name) {
        return Sponge.getServer().getPlayer(name).orElse(null);
    }

    @Override
    public void kickPlayer(String player, String reason) {
        Text reasonText = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(reason);
        Sponge.getServer().getPlayer(player).ifPresent(p->p.kick(reasonText));
    }

    @Override
    public Object[] getOnlinePlayers() {
        return Sponge.getServer().getOnlinePlayers().toArray();
    }

    @Override
    public void scheduleAsyncRep(Runnable rn, long l1, long l2) {
        Sponge.getScheduler().createTaskBuilder().async().delayTicks(l1).intervalTicks(l2).submit(plugin);
    }

    @Override
    public void scheduleAsync(Runnable rn, long l1) {
        Sponge.getScheduler().createTaskBuilder().execute(rn).async().delayTicks(l1).submit(plugin);
    }

    @Override
    public void runAsync(Runnable rn) {
        Sponge.getScheduler().createTaskBuilder().execute(rn).async().submit(plugin);
    }

    @Override
    public void runSync(Runnable rn) {
        Sponge.getScheduler().createTaskBuilder().execute(rn).submit(plugin);
    }

    @Override
    public void executeCommand(String cmd) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(),cmd);
    }

    @Override
    public boolean callChat(Object player) {
        Punishment pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)));
        if (pnt != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean callCMD(Object player, String cmd) {
        Punishment pnt;
        if (Universal.get().isMuteCommand(cmd.split(" ")[0].substring(1)) && (pnt = PunishmentManager.get().getMute(UUIDManager.get().getUUID(getName(player)))) != null) {
            for (String str : pnt.getLayout()) {
                sendMessage(player, str);
            }
            return true;
        }
        return false;
    }

    @Override
    public void loadMySQLFile(File f) {
        mysqlLoader = YAMLConfigurationLoader.builder().setFile(f).build();
        try {
            mysqlNode = mysqlLoader.load();
        } catch (IOException e) {
            plugin.logger.error("Failed to load MySQL config.",e);
        }
    }

    @Override
    public void createMySQLFile(File f) {
        //todo:file?
        mysqlNode.getNode("MySQL","IP").setValue("localhost");
        mysqlNode.getNode("MySQL","DB-Name").setValue("YourDatabase");
        mysqlNode.getNode("MySQL","Username").setValue("root");
        mysqlNode.getNode("MySQL","Password").setValue("pw123");
        mysqlNode.getNode("MySQL","Port").setValue(3306);
        try {
            mysqlLoader.save(messageNode);
        } catch (IOException e) {
            plugin.logger.error("Failed to save default MySQL config.",e);
        }
    }

    @Override
    public Object getMySQLFile() {
        return messageNode;
    }

    @Override
    public String parseJSON(InputStreamReader json, String key) {

        try {
            return new JsonParser().parse(json).getAsJsonObject().get(key).toString();
        } catch (JsonParseException e) {
            plugin.logger.error("Error -> ",e);
            return null;
        }
    }

    @Override
    public String parseJSON(String json, String key) {
        try {
            return new JsonParser().parse(json).getAsJsonObject().get(key).toString();
        } catch (JsonParseException e) {
            return null;
        }
    }

    @Override
    public Boolean getBoolean(Object file, String path) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getBoolean();
    }

    @Override
    public String getString(Object file, String path) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getString();
    }

    @Override
    public Long getLong(Object file, String path) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getLong();
    }

    @Override
    public Integer getInteger(Object file, String path) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getInt();
    }

    @Override
    public List<String> getStringList(Object file, String path) {
        try {
            return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getList(TypeToken.of(String.class), Collections.emptyList());
        } catch (ObjectMappingException e) {
            plugin.logger.error("",e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getBoolean(def);
    }

    @Override
    public String getString(Object file, String path, String def) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getString(def);
    }

    @Override
    public long getLong(Object file, String path, long def) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getLong(def);
    }

    @Override
    public int getInteger(Object file, String path, int def) {
        return ((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).getInt(def);
    }

    @Override
    public boolean contains(Object file, String path) {
        return !((ConfigurationNode)file).getNode((Object[]) path.split("\\.")).isVirtual();
    }

    @Override
    public String getFileName(Object file) {
        return ((ConfigurationNode)file).getKey().toString();
    }

    @Override
    public void callPunishmentEvent(Punishment punishment) {
        try {
            Sponge.getEventManager().post(new PunishmentEvent(punishment,getCause()));
        } catch (Throwable throwable) {
            plugin.logger.error("Failed to post PunishmentEvent",throwable);
        }
    }

    @Override
    public void callRevokePunishmentEvent(Punishment punishment, boolean massClear) {
        try {
            Sponge.getEventManager().post(new RevokePunishmentEvent(punishment,massClear,getCause()));
        } catch (Throwable throwable) {
            plugin.logger.error("Failed to post RevokePunishmentEvent",throwable);
        }
    }

    @Override
    public boolean isOnlineMode() {
        return Sponge.getServer().getOnlineMode();
    }

    @Override
    public void notify(String perm, List<String> notification) {
        for(Player p:Sponge.getServer().getOnlinePlayers()){
            if(hasPerms(p,perm)){
                for(String str:notification){
                    sendMessage(p,str);
                }
            }
        }
    }

    @Override
    public void log(String msg) {
        plugin.logger.info(msg);
    }

    //能用就行
    private MethodHandle causeBuilder;
    private MethodHandle causeBuilderBuild;
    private boolean api6 = false;
    private Object emptyContext;

    private Cause getCause() throws Throwable {
        if(causeBuilderBuild == null){
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                causeBuilder = lookup.findVirtual(Cause.Builder.class,"append",MethodType.methodType(
                        Cause.Builder.class,
                        Object.class
                ));
                causeBuilderBuild = lookup.findVirtual(Cause.Builder.class,"build", MethodType.methodType(
                        Cause.class,
                        Class.forName("org.spongepowered.api.event.cause.EventContext")
                ));
                Class<?> eventContextClass = Class.forName("org.spongepowered.api.event.cause.EventContext");
                Field field = eventContextClass.getDeclaredField("EMPTY_CONTEXT");
                field.setAccessible(true);
                emptyContext = field.get(null);
            } catch (ReflectiveOperationException e) {
                api6 = true;
                causeBuilder = lookup.findVirtual(Cause.Builder.class,"named",
                        MethodType.methodType(Cause.Builder.class,String.class,Object.class)
                );
                causeBuilderBuild = lookup.findVirtual(Cause.Builder.class,"build", MethodType.methodType(Cause.class));
            }
        }
        if(api6){
            return (Cause) causeBuilderBuild.invoke(causeBuilder.invoke(Cause.builder(),"Source",plugin));
        }else {
            return (Cause) causeBuilderBuild.invoke(causeBuilder.invoke(Cause.builder(),plugin),emptyContext);
        }
    }

}
