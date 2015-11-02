package info.servertools.core.commands;

import info.servertools.core.STCommand;
import info.servertools.core.util.ServerUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import javax.annotation.Nullable;
import java.util.List;

public class CommandWhereIs extends STCommand {

    public CommandWhereIs(final String defaultName) {
        super(defaultName);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <player>";
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        if (args.length <= 1) {
            return getListOfStringsMatchingLastWord(args, ServerUtils.getAllUsernames());
        } else {
            return null;
        }
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length == 1) {
            final EntityPlayerMP player = getPlayer(sender, args[0]);
            sender.addChatMessage(new ChatComponentText(
                    player.getGameProfile().getName() +
                            " is at X: " + player.posX +
                            " Y: " + player.posY +
                            " Z: " + player.posZ +
                            " in Dim: " + player.worldObj.provider.getDimensionId() + " (" + player.worldObj.provider.getDimensionName() + ')'
            ));

        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
