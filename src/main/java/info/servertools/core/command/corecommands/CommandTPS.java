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

import static net.minecraft.util.EnumChatFormatting.AQUA;
import static net.minecraft.util.EnumChatFormatting.GOLD;
import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.LIGHT_PURPLE;
import static net.minecraft.util.EnumChatFormatting.RESET;
import static net.minecraft.util.EnumChatFormatting.WHITE;

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.util.ChatMessage;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.text.DecimalFormat;

import javax.annotation.Nullable;

public class CommandTPS extends ServerToolsCommand {

    private static final DecimalFormat timeFormatter = new DecimalFormat("########0.000");

    public CommandTPS(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {
        return CommandLevel.ANYONE;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + name + " {DIM}";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        switch (args.length) {
            case 0:
                tpsSummary(sender);
                break;
            case 1:
                tpsDimension(sender, parseInt(args[0]));
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    private static void tpsDimension(ICommandSender sender, int dimId) throws CommandException {
        @Nullable World world = DimensionManager.getWorld(dimId);
        if (world == null) {
            throw new CommandException("That dimension doesn't exist or isn't loaded");
        }

        final double dimensionTickTime = mean(MinecraftServer.getServer().worldTickTimes.get(dimId)) * 1.0E-6D;
        final double dimensionTPS = Math.min(1000.0 / dimensionTickTime, 20);

        sender.addChatMessage(ChatMessage.builder().color(GOLD).add("TPS Report for dimension: ")
                                      .color(AQUA).add(String.format("%d ", dimId))
                                      .color(GOLD).add("(").color(WHITE).add(world.provider.getDimensionName()).color(GOLD).add(")").build());

        sender.addChatMessage(ChatMessage.builder().color(GREEN).add(" Mean tick time: ").color(AQUA).add(timeFormatter.format(dimensionTickTime)).color(RESET).add(" ms").build());
        sender.addChatMessage(ChatMessage.builder().color(GREEN).add(" Mean TPS: ").color(AQUA).add(timeFormatter.format(dimensionTPS)).build());
        sender.addChatMessage(ChatMessage.builder().add(" Loaded chunks: ").color(AQUA).add(String.valueOf(world.getChunkProvider().getLoadedChunkCount())).build());
        sender.addChatMessage(ChatMessage.builder().add(" Loaded entities: ").color(AQUA).add(String.valueOf(world.loadedEntityList.size())).build());
        sender.addChatMessage(ChatMessage.builder().add("    Players: ").color(AQUA).add(String.valueOf(world.playerEntities.size())).build());
        sender.addChatMessage(ChatMessage.builder().add(" Loaded tile entities: ").color(AQUA).add(String.valueOf(world.loadedTileEntityList.size())).build());
    }

    private static void tpsSummary(ICommandSender sender) {
        sender.addChatMessage(ChatMessage.builder().color(GOLD).add("-- Overall TPS Report --").build());

        for (final int dimID : DimensionManager.getIDs()) {
            final double dimensionTickTime = mean(MinecraftServer.getServer().worldTickTimes.get(dimID)) * 1.0E-6D;
            final double dimensionTPS = Math.min(1000.0 / dimensionTickTime, 20);
            sender.addChatMessage(ChatMessage.builder()
                                          .color(GOLD).add(String.format("Dim %d: ", dimID)).color(GREEN).add("Tick time: ").color(AQUA).add(timeFormatter.format(dimensionTickTime))
                                          .color(GREEN).add(" TPS: ").color(AQUA).add(timeFormatter.format(dimensionTPS))
                                          .build());
        }

        final double meanTickTime = mean(MinecraftServer.getServer().tickTimeArray) * 1.0E-6D;
        final double meanTPS = Math.min(1000.0 / meanTickTime, 20);

        sender.addChatMessage(ChatMessage.builder()
                                      .color(LIGHT_PURPLE).add("Server Average: ").color(GREEN).add("Tick time: ").color(AQUA).add(timeFormatter.format(meanTickTime))
                                      .color(GREEN).add(" TPS: ").color(AQUA).add(timeFormatter.format(meanTPS))
                                      .build());
    }

    private static long mean(long[] values) {
        long sum = 0l;
        for (long val : values) {
            sum += val;
        }
        return sum / values.length;
    }
}
