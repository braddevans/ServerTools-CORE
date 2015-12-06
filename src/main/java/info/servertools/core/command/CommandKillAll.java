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
package info.servertools.core.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import com.google.common.base.Joiner;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

public class CommandKillAll extends STCommand {

    public CommandKillAll() {
        super("killall");
        setPermissionLevel(PERMISSION_OPERATOR);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " <entity>";
    }

    @Override
    @Nullable
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        return args.length <= 1 ? getListOfStringsMatchingLastWord(args, EntityList.stringToClassMapping.keySet()) : null;
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {

        if (args.length < 1)
            throw new WrongUsageException(getCommandUsage(sender));

        Optional<String> targetName = EntityList.stringToClassMapping.keySet().stream()
                .filter(entityName -> entityName.equalsIgnoreCase(Joiner.on(' ').join(args)))
                .findAny();

        if (!targetName.isPresent()) {
            throw new CommandException("That entity type doesn't exist");
        }

        final String entityName = targetName.get();

        final AtomicInteger removed = new AtomicInteger(0); // Boo. Can't increment ints within a foreach
        for (World world : MinecraftServer.getServer().worldServers) {
            world.loadedEntityList.stream()
                    .filter(entity -> !(entity instanceof EntityPlayerMP))
                    .filter(entity -> EntityList.getEntityString(entity).equalsIgnoreCase(entityName))
                    .forEach(entity -> {
                        world.removeEntity(entity);
                        removed.incrementAndGet();
                    });
        }

        notifyOperators(sender, this, "Removed %s instances of %s", removed.intValue(), entityName);
    }
}
