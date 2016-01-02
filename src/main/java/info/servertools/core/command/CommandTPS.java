/*
 * This file is a part of ServerTools <http://servertools.info>
 *
 * Copyright (c) 2015 ServerTools
 * Copyright (c) 2015 contributors
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

import static net.minecraft.util.EnumChatFormatting.AQUA;

import info.servertools.core.util.ServerUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import net.minecraftforge.common.DimensionManager;

import java.text.DecimalFormat;

@Command(
        name = "tps",
        opRequired = false
)
public class CommandTPS extends STCommand {

    private static final DecimalFormat format = new DecimalFormat("########0.000");

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        sender.addChatMessage(new ChatComponentText(AQUA + "-- TPS Sumary --"));
        for (int dimId : DimensionManager.getIDs()) {
            final String tickTime = format.format(ServerUtils.getWorldTickTime(dimId));
            final String tps = format.format(ServerUtils.getWorldTickTime(dimId));
            sender.addChatMessage(new ChatComponentText("    Dim: " + dimId + " Tick Time: " + tickTime + " TPS: " + tps));
        }
        final String avgTickTime = format.format(ServerUtils.getMeanTickTime());
        final String avgTps = format.format(ServerUtils.getMeanTPS());
        sender.addChatMessage(new ChatComponentText("  Mean Tick Time: " + avgTickTime + " Mean TPS: " + avgTps));
    }
}
