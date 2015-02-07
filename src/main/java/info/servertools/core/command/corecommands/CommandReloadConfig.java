package info.servertools.core.command.corecommands;

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.config.STConfig;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CommandReloadConfig extends ServerToolsCommand {

    public CommandReloadConfig(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.OP;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        STConfig.load();
        notifyOperators(sender, this, "Reloaded ServerTools configuration");
    }
}
