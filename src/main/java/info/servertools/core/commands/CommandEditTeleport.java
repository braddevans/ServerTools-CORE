package info.servertools.core.commands;

import info.servertools.core.STCommand;
import info.servertools.core.feature.TeleportHandler;
import info.servertools.core.util.Location;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public class CommandEditTeleport extends STCommand {

    private final TeleportHandler teleportHandler;

    public CommandEditTeleport(final TeleportHandler teleportHandler, final String defaultName) {
        super(defaultName);
        this.teleportHandler = teleportHandler;
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <set|delete> <name> [[dim] [x] [y] [z]]";
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        switch (args.length) {
            case 0:
            case 1:
                return getListOfStringsMatchingLastWord(args, "set", "delete");
            case 2:
                return "delete".equals(args[0]) ? getListOfStringsMatchingLastWord(args, teleportHandler.getTeleportNames()) : null;
            default:
                return null;
        }
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        switch (args.length) {
            case 2:
                if ("delete".equals(args[0])) {
                    final TeleportHandler.EditTeleportResult result = teleportHandler.deleteTeleport(args[1]);
                    sender.addChatMessage(result.toChat());
                } else if ("set".equals(args[0])) {
                    final EntityPlayerMP player = requirePlayer(sender, "Error: provide coordinates or execute as a player");
                    final Location teleport = new Location(player);
                    final TeleportHandler.EditTeleportResult result = teleportHandler.setTeleport(args[1], teleport);
                    player.addChatMessage(result.toChat());
                } else {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                break;
            case 5:
                if ("set".equals(args[0])) {
                    final Location teleport = new Location(
                            parseInt(args[2]), // Dim
                            parseDouble(args[3]), // X
                            parseDouble(args[4]), // Y
                            parseDouble(args[5])); // Z
                    final TeleportHandler.EditTeleportResult result = teleportHandler.setTeleport(args[1], teleport);
                    sender.addChatMessage(result.toChat());
                } else {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
