package info.servertools.core.chat;

import static net.minecraft.util.EnumChatFormatting.GRAY;

import info.servertools.core.util.ServerUtils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class OPChatFormatter {

    private final IChatComponent opPrefix;

    public OPChatFormatter() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        opPrefix = new ChatComponentText("[OP]");
        opPrefix.getChatStyle().setColor(GRAY);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!MinecraftServer.getServer().isSinglePlayer() && ServerUtils.isOP(event.player.getGameProfile())) {
            event.player.addPrefix(opPrefix);
        }
    }
/* TODO: Find a way around the fact that CommandEvents are fired before the command is executed
    @SubscribeEvent
    public void onCommand(final CommandEvent event) {
        if (event.command instanceof CommandOp || event.command instanceof CommandDeOp) {
            for (final EntityPlayerMP player : ServerUtils.getAllPlayers()) {
                if ((ServerUtils.isMultiplayer() && ServerUtils.isOP(player.getGameProfile())) && !player.getPrefixes().contains(opPrefix)) {
                    player.addPrefix(opPrefix);
                } else {
                    player.getPrefixes().remove(opPrefix);
                }
            }
        }
    }
*/
}
