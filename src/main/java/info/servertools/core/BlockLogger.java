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
package info.servertools.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import info.servertools.core.lib.Reference;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockLogger {

    private static final Logger log = LogManager.getLogger();

    private static final String FILE_HEADER = "TimeStamp,UUID,DimID,BlockX,BlockY,BlockZ,BlockName";

    private final Path breakDirectory;
    private final Lock breakFileLock = new ReentrantLock();

    private final Path placeDirectory;
    private final Lock placeFileLock = new ReentrantLock();

    private final boolean logBlockBreaks, logBlockPlaces;

    public BlockLogger(final Path logDirectory, final boolean logBlockBreaks, final boolean logBlockPlaces) {
        checkNotNull(logDirectory);
        this.breakDirectory = logDirectory.resolve("breaks");
        this.placeDirectory = logDirectory.resolve("places");
        this.logBlockBreaks = logBlockBreaks;
        this.logBlockPlaces = logBlockPlaces;
        checkArgument(!Files.exists(logDirectory) || Files.isDirectory(logDirectory), "File exists with the name: " + logDirectory);
        checkArgument(!Files.exists(breakDirectory) || Files.isDirectory(breakDirectory), "File exists with the name: " + breakDirectory);
        checkArgument(!Files.exists(placeDirectory) || Files.isDirectory(placeDirectory), "File exists with the name: " + placeDirectory);

        MinecraftForge.EVENT_BUS.register(this);

        try {
            Files.createDirectories(breakDirectory);
            Files.createDirectories(placeDirectory);
        } catch (IOException e) {
            log.error("Failed to create logging directories", e);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(final BlockEvent.BreakEvent event) {
        if (!logBlockBreaks) {
            return;
        }
        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                breakFileLock.lock();
                final Path logFile = getLogFile(breakDirectory);
                final boolean writeHeader = !Files.exists(logFile);

                try (final BufferedWriter writer = Files.newBufferedWriter(logFile, Reference.CHARSET, CREATE, APPEND)) {
                    if (writeHeader) {
                        writer.append(FILE_HEADER + Reference.LINE_SEPARATOR);
                    }
                    writer.append(String.format("%s,%s,%s,%s,%s,%s,%s",
                                                getTimeStamp(),
                                                event.getPlayer().getPersistentID(),
                                                event.world.provider.getDimensionId(),
                                                event.pos.getX(),
                                                event.pos.getY(),
                                                event.pos.getZ(),
                                                event.state.getBlock().getUnlocalizedName()
                    )).append(Reference.LINE_SEPARATOR);
                } catch (IOException e) {
                    log.error("Failed to save logFile " + logFile, e);
                } finally {
                    breakFileLock.unlock();
                }
            }
        });
    }

    @SubscribeEvent
    public void onBlockPlace(final BlockEvent.PlaceEvent event) {
        if (!logBlockPlaces) {
            return;
        }

        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                placeFileLock.lock();
                final Path logFile = getLogFile(placeDirectory);
                final boolean writeHeader = !Files.exists(logFile);

                try (final BufferedWriter writer = Files.newBufferedWriter(logFile, Reference.CHARSET, CREATE, APPEND)) {
                    if (writeHeader) {
                        writer.append(FILE_HEADER + Reference.LINE_SEPARATOR);
                    }
                    writer.append(String.format("%s,%s,%s,%s,%s,%s,%s",
                                                getTimeStamp(),
                                                event.player.getPersistentID(),
                                                event.world.provider.getDimensionId(),
                                                event.pos.getX(),
                                                event.pos.getY(),
                                                event.pos.getZ(),
                                                event.state.getBlock().getUnlocalizedName()
                    )).append(Reference.LINE_SEPARATOR);
                } catch (IOException e) {
                    log.error("Failed to save logFile " + logFile, e);
                } finally {
                    placeFileLock.unlock();
                }
            }
        });
    }

    private static Path getLogFile(final Path directory) {
        return directory.resolve(new SimpleDateFormat("MM-dd-YYYY").format(new Date()) + ".csv");
    }

    private static String getTimeStamp() {
        return new SimpleDateFormat("kk-mm-ss").format(new Date());
    }
}
