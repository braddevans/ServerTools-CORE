package info.servertools.core.command.corecommands;

import static net.minecraft.util.EnumChatFormatting.GRAY;
import static net.minecraft.util.EnumChatFormatting.RED;

import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.teleport.BackHandler;
import info.servertools.core.util.ChatMessage;
import info.servertools.core.util.Location;
import info.servertools.core.util.ServerUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;

public class CommandBack extends ServerToolsCommand {

    public CommandBack(final String defaultName) {
        super(defaultName);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + name;
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        final EntityPlayerMP player = requirePlayer(sender);
        @Nullable final Location backLocation = BackHandler.instance().getBackLocation(player.getPersistentID());
        if (backLocation != null) {
            if (player.dimension == backLocation.dimID) {
                ServerUtils.teleportPlayer(player, backLocation);
                player.addChatMessage(ChatMessage.builder().color(GRAY).add("Teleported back").build());
            } else {
                player.addChatMessage(ChatMessage.builder().color(RED).add("Your back location is in another world").build());
            }
        } else {
            player.addChatMessage(ChatMessage.builder().color(RED).add("You don't have a back location").build());
        }
    }
}
