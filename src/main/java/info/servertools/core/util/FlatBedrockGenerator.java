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
package info.servertools.core.util;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class FlatBedrockGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider iChunkProvider, IChunkProvider iChunkProvider2) {

        BiomeGenBase biomeGenBase = world.getBiomeGenForCoords(new BlockPos(chunkX, 0, chunkZ));
        boolean isNether = "hell".equals(biomeGenBase.biomeName.toLowerCase());

        for (int blockX = 0; blockX < 16; blockX++) {
            for (int blockZ = 0; blockZ < 16; blockZ++) {

                if (isNether) {
                    for (int blockY = 126; blockY > 121; blockY--) {
                        final BlockPos pos = new BlockPos(chunkX * 16 + blockX, blockY, chunkZ * 16 + blockZ);
                        if (world.getBlockState(pos).getBlock() == Blocks.bedrock) {
                            world.setBlockState(pos, Blocks.netherrack.getDefaultState(), 2);
                        }
                    }
                }

                for (int blockY = 5; blockY > 0; blockY--) {
                    final BlockPos pos = new BlockPos(chunkX * 16 + blockX, blockY, chunkZ * 16 + blockZ);
                    if (world.getBlockState(pos).getBlock() == Blocks.bedrock) {
                        if (isNether) {
                            world.setBlockState(pos, Blocks.netherrack.getDefaultState(), 2);
                        } else {
                            world.setBlockState(pos, Blocks.stone.getDefaultState(), 2);
                        }
                    }
                }

            }
        }
    }
}
