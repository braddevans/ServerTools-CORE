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
package info.servertools.core.command.corecommands;

import static info.servertools.core.command.CommandLevel.OP;
import static net.minecraft.util.EnumChatFormatting.AQUA;
import static net.minecraft.util.EnumChatFormatting.GOLD;
import static net.minecraft.util.EnumChatFormatting.YELLOW;

import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.ChatMessage;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

import javax.annotation.Nullable;

public class CommandEntityCount extends ServerToolsCommand {

    public CommandEntityCount(String defaultName) {
        super(defaultName);
        setRequiredLevel(OP);
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, (String[]) EntityList.classToStringMapping.values().toArray()) : null;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " {DIMID}";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(final ICommandSender sender, String[] args) throws CommandException {

        WorldServer worldServer;

        switch (args.length) {
            case 0:
                if (sender instanceof EntityPlayerMP) {
                    worldServer = ((EntityPlayerMP) sender).getServerForPlayer();
                } else {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                break;
            case 1:
                @Nullable WorldServer world = DimensionManager.getWorld(parseInt(args[0]));
                if (world != null) {
                    worldServer = world;
                } else {
                    throw new CommandException("That dimension doesn't exist or isn't loaded");
                }
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }

        final TObjectIntHashMap<String> counts = new TObjectIntHashMap<>();

        for (Entity entity : (List<Entity>) worldServer.loadedEntityList) {
            final String name = EntityList.getEntityString(entity);
            if (!counts.containsKey(name)) {
                counts.put(name, 1);
            } else {
                counts.increment(name);
            }
        }

        sender.addChatMessage(ChatMessage.builder().color(GOLD).add("Loaded entities: ").color(AQUA).add(String.format("%d", worldServer.loadedEntityList.size())).build());
        counts.forEachEntry(new TObjectIntProcedure<String>() {
            @Override
            public boolean execute(String name, int count) {
                sender.addChatMessage(ChatMessage.builder().add("  ").color(YELLOW).add(name + ": ").color(AQUA).add(String.format("%d", count)).build());
                return true;
            }
        });
    }
}
