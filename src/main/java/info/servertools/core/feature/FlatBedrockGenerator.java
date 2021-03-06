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
package info.servertools.core.feature;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;
import java.util.stream.IntStream;

public class FlatBedrockGenerator implements IWorldGenerator {

    @Override
    public void generate(final Random random, final int chunkX, final int chunkZ, final World world,
                         final IChunkProvider chunkGenerator, final IChunkProvider chunkProvider) {

        final boolean isNether = "hell".equals(world.getBiomeGenForCoords(new BlockPos(chunkX, 0, chunkZ)).biomeName.toLowerCase());

        IntStream.range(0, 16).forEach(x -> IntStream.range(0, 16).forEach(z -> {

            IntStream.range(1, 5).forEach(y -> {
                final BlockPos pos = new BlockPos(chunkX * 16 + x, y, chunkZ * 16 + z);
                if (world.getBlockState(pos).getBlock() == Blocks.bedrock) {
                    world.setBlockState(pos, isNether ? Blocks.netherrack.getDefaultState() : Blocks.stone.getDefaultState(), 2);
                }
            });

            if (isNether) {
                IntStream.range(121, 127).forEach(y -> {
                    final BlockPos pos = new BlockPos(chunkX * 16 + x, y, chunkZ * 16 + z);
                    if (world.getBlockState(pos).getBlock() == Blocks.bedrock) {
                        world.setBlockState(pos, Blocks.netherrack.getDefaultState(), 2);
                    }
                });
            }
        }));
    }
}
