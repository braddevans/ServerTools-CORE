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

import info.servertools.core.ServerTools;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.task.RemoveAllTickTask;

import gnu.trove.set.hash.THashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.registry.GameData;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class CommandRemoveAll extends ServerToolsCommand {

    public CommandRemoveAll(String defaultName) {
        super(defaultName);
        setRequiredLevel(OP);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {

        return "/" + name + " [blockName] {radius} <-u>";
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, GameData.getBlockRegistry().getKeys()) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(ICommandSender sender, String[] strings) throws CommandException {

        if (!(sender instanceof EntityPlayerMP)) { throw new WrongUsageException("Only players can use that command"); }

        if (strings.length < 1) { throw new WrongUsageException(getCommandUsage(sender)); }
        int range = 15;
        boolean blockUpdate = false;

        if (ArrayUtils.contains(strings, "-u")) {
            blockUpdate = true;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (strings.length >= 2 && !"-u".equals(strings[1])) {
            range = parseInt(strings[1]);
        }

        Set<Block> blocksToClear = new THashSet<>();

        if ("fluid".equals(strings[0])) {
            for (final Object block : GameData.getBlockRegistry()) {
                if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
                    blocksToClear.add((Block) block);
                }
            }
        } else {
            blocksToClear.add(getBlockByText(sender, strings[0]));
        }

        ServerTools.instance.tickHandler.registerTask(new RemoveAllTickTask(player, range, blocksToClear, blockUpdate));
    }
}
