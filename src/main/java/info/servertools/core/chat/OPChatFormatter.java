/*
 * This file is a part of ServerTools <http://servertools.info>
 *
 * Copyright (c) 2014 ServerTools
 * Copyright (c) 2014 contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
