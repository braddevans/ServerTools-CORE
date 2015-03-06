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
package info.servertools.core.task;

import static net.minecraft.util.EnumChatFormatting.GOLD;

import info.servertools.core.util.ChatMessage;
import info.servertools.core.util.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class RemoveAllTickTask implements ITickTask {

    private static final int BLOCKS_PER_TICK = 500;
    private static final int LAG_THREASHOLD = 1500;

    private boolean isComplete;
    private final EntityPlayer player;
    private final Collection<BlockPos> blocksToRemove;
    private final World world;
    private int blockCounter;
    private boolean blockUpdate;

    public RemoveAllTickTask(EntityPlayer player, int radius, Collection<Block> blocksToClear, boolean blockUpdate) {

        this.player = player;
        world = player.worldObj;
        this.blockUpdate = blockUpdate;

        int centerX = (int) player.posX;
        int centerY = (int) player.posY;
        int centerZ = (int) player.posZ;

        blocksToRemove = new ArrayList<>();

        for (int x = centerX - radius; x < centerX + radius; x++) {
            for (int y = centerY - radius; y < centerY + radius; y++) {
                for (int z = centerZ - radius; z < centerZ + radius; z++) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    if (blocksToClear.contains(world.getBlockState(pos).getBlock())) {
                        blocksToRemove.add(pos);
                        blockCounter++;
                    }
                }
            }
        }

        player.addChatComponentMessage(ChatMessage.builder()
                                               .color(GOLD).add("Removing ").color(EnumChatFormatting.AQUA).add(String.valueOf(blockCounter)).color(GOLD).add(" blocks")
                                               .build());

        if (blockCounter > LAG_THREASHOLD) {
            player.addChatMessage(ChatMessage.builder().color(EnumChatFormatting.RED).add("Removing large amount of blocks").build());
        }
    }

    @Override
    public void tick() {
        if (blocksToRemove.isEmpty()) {
            isComplete = true;
            player.addChatComponentMessage(ChatUtils.getChatComponent("Finished removing blocks", EnumChatFormatting.GREEN));
            return;
        }

        Iterator<BlockPos> iterator = blocksToRemove.iterator();

        for (int i = 0; i < BLOCKS_PER_TICK; i++) {
            if (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                world.setBlockState(pos, Blocks.air.getDefaultState(), blockUpdate ? 3 : 2);
                iterator.remove();
            }
        }
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }
}
