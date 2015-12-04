package info.servertools.core.command;

import info.servertools.core.util.ServerUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

import java.util.List;

import javax.annotation.Nullable;

public class CommandHeal extends STCommand {

    public CommandHeal() {
        super("heal");
        setPermissionLevel(PERMISSION_OPERATOR);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " [player]";
    }

    @Override
    public boolean isUsernameIndex(final String[] args, final int index) {
        return index == 0;
    }

    @Override
    @Nullable
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        return args.length <= 1 ? getListOfStringsMatchingLastWord(args, ServerUtils.getAllUsernames()) : null;
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {

        EntityPlayerMP player;
        if (args.length == 0) {
            player = getCommandSenderAsPlayer(sender);
        } else if (args.length == 1) {
            player = getPlayer(sender, args[0]);
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        player.heal(Float.MAX_VALUE);
    }
}
