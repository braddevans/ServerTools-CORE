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

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommandSpawnMob extends ServerToolsCommand {

    public CommandSpawnMob(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {

        return CommandLevel.OP;
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<?> var = getValidEntities();
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, (String[]) var.toArray()) : null;
    }

    @SuppressWarnings("unchecked")
    private static List<?> getValidEntities() {
        List<String> ret = new ArrayList<>();
        for (String name : ((Map<String, Class<?>>) EntityList.stringToClassMapping).keySet()) {
            Class<?> clazz = (Class<?>) EntityList.stringToClassMapping.get(name);
            if (EntityLiving.class.isAssignableFrom(clazz)) { ret.add(name); }
        }
        return ret;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {

        return "/" + name + " [mobname] {ammount}";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        if (!(sender instanceof EntityPlayer)) { throw new WrongUsageException("Only players can use that command"); }

        EntityPlayer player = (EntityPlayer) sender;

        if (args.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        int amount = 1;
        if (args.length > 1) {
            amount = parseInt(args[1], 1, 100);
        }

        @Nullable Class<?> clazz = null;
        String type = "Unknown";
        for (String name : ((Map<String, Class<?>>) EntityList.stringToClassMapping).keySet()) {
            if (name.equalsIgnoreCase(args[0])) {
                clazz = (Class<?>) EntityList.stringToClassMapping.get(name);
                type = name;
                break;
            }
        }
        if (clazz == null || !EntityLiving.class.isAssignableFrom(clazz)) {
            throw new CommandException("That entity type doesn't exist. Try using tab completion");
        }

        try {
            Constructor<?> ctor = clazz.getConstructor(World.class);
            for (int i = 0; i < amount; i++) {
                Entity ent = (Entity) ctor.newInstance(player.worldObj);
                ent.setPosition(player.posX, player.posY, player.posZ);
                player.worldObj.spawnEntityInWorld(ent);
            }
        } catch (Throwable e) {
            throw new CommandException("Failed to spawn entity");
        }

        notifyOperators(sender, this, "Spawned " + amount + " " + type);
    }
}
