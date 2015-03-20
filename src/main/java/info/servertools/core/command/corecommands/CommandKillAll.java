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

import static com.google.common.base.Strings.isNullOrEmpty;
import static info.servertools.core.command.CommandLevel.OP;

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class CommandKillAll extends ServerToolsCommand {

    public CommandKillAll(String defaultName) {
        super(defaultName);
        setRequiredLevel(OP);
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, EntityList.stringToClassMapping.keySet()) : null;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " [entity]";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        if (args.length != 1) { throw new WrongUsageException(getCommandUsage(sender)); }

        @Nullable String target = null;

        for (String name : (Set<String>) EntityList.stringToClassMapping.keySet()) {
            if (name.equalsIgnoreCase(args[0])) {
                target = name;
                break;
            }
        }

        if (isNullOrEmpty(target)) {
            throw new CommandException("That entity type doesn't exist. Try using tab completion");
        }

        int removed = 0;
        for (World world : MinecraftServer.getServer().worldServers) {
            for (Entity entity : (List<Entity>) world.loadedEntityList) {
                if ((!(entity instanceof EntityPlayer))) {
                    @Nullable String entityName = EntityList.getEntityString(entity);
                    if (entityName != null && entityName.equals(target)) {
                        world.removeEntity(entity);
                        removed++;
                    }
                }
            }
        }

        notifyOperators(sender, this, "Removed " + removed + " entities of type: " + target);
    }
}
