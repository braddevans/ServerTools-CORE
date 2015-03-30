package info.servertools.core.command.corecommands;

import static net.minecraft.util.EnumChatFormatting.RED;

import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.ChatMessage;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandTop extends ServerToolsCommand {

    public CommandTop(final String defaultName) {
        super(defaultName);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + name;
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        final EntityPlayerMP player = requirePlayer(sender);
        final World world = player.worldObj;

        BlockPos blockPos = new BlockPos(player.posX, player.posY, player.posZ);
        for (; ; ) {
            if (world.getBlockState(blockPos).getBlock() == Blocks.bedrock) {
                player.addChatMessage(ChatMessage.builder().color(RED).add("Top of the world couldn't be found").build());
                return;
            }
            if (world.isAirBlock(blockPos) && world.canBlockSeeSky(blockPos)) {
                break;
            }
            blockPos = blockPos.up();
        }

        player.setPositionAndUpdate(player.posX, blockPos.getY(), player.posZ);
    }
}
