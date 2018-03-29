package me.leoko.advancedban.sponge.listener;

import me.leoko.advancedban.manager.CommandManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandReceiverSponge implements CommandExecutor {
    public static CommandElement args = new Args();

    private String name;

    public CommandReceiverSponge(String name) {
        this.name = name;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CommandManager.get().onCommand(src,name,args.<String[]>getOne("args").get());
        return CommandResult.success();
    }

    public static class Args extends CommandElement{

        protected Args() {
            super(Text.of("args"));
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            List<String> stringList = new ArrayList<>();
            while (args.hasNext()){
                stringList.add(args.next());
            }
            return stringList.toArray(new String[]{});
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return Collections.emptyList();
        }
    }
}
