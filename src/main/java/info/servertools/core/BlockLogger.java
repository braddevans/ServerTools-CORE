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

import info.servertools.core.lib.Reference;
import info.servertools.core.util.SaveThread;

import com.google.common.io.Files;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockLogger {

    private static final String FILE_HEADER = "TimeStamp,UUID,DimID,BlockX,BlockY,BlockZ,BlockName";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-YYYY");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("kk-mm-ss");

    private final File breakDirectory;
    private final Lock breakFileLock = new ReentrantLock();

    private final File placeDirectory;
    private final Lock placeFileLock = new ReentrantLock();

    private final boolean logBlockPlaces;
    private final boolean logBlockBreaks;


    public BlockLogger(File breakDirectory, boolean logBlockBreaks, File placeDirectory, boolean logBlockPlaces) {
        this.logBlockBreaks = logBlockBreaks;
        this.logBlockPlaces = logBlockPlaces;
        this.breakDirectory = breakDirectory;
        this.placeDirectory = placeDirectory;
        if (logBlockBreaks) {
            if (breakDirectory.exists() && !breakDirectory.isDirectory()) {
                throw new IllegalArgumentException("File with same name as block break logging directory detected");
            }
            breakDirectory.mkdirs();
        }
        if (logBlockPlaces) {
            if (placeDirectory.exists() && !placeDirectory.isDirectory()) {
                throw new IllegalArgumentException("File with same name as block place logging directory detected");
            }
            placeDirectory.mkdirs();
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!logBlockBreaks) { return; }
        final File logFile = new File(breakDirectory, DATE_FORMAT.format(Calendar.getInstance().getTime()) + ".csv");
        new SaveThread(String.format(
                "%s,%s,%s,%s,%s,%s,%s",
                TIME_FORMAT.format(Calendar.getInstance().getTime()), //When was it placed
                event.getPlayer().getPersistentID(), // Who placed it (UUID)
                event.world.provider.getDimensionId(), // What dimension was it in
                event.pos.getX(), // XCoord
                event.pos.getY(), // YCoord
                event.pos.getZ(), // ZCoord
                event.state.getBlock().getUnlocalizedName() // What block was it

        ) + Reference.LINE_SEPARATOR) {
            @Override
            public void run() {
                breakFileLock.lock();
                try {
                    if (!logFile.exists()) {
                        writeHeader(logFile);
                    }
                    Files.append(data, logFile, Reference.CHARSET);
                } catch (IOException e) {
                    super.log.warn("Failed to save block break file to disk", e);
                } finally {
                    breakFileLock.unlock();
                }
            }
        }.start();
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (!logBlockPlaces) { return; }
        final File logFile = new File(placeDirectory, DATE_FORMAT.format(Calendar.getInstance().getTime()) + ".csv");
        new SaveThread(String.format(
                "%s,%s,%s,%s,%s,%s,%s",
                TIME_FORMAT.format(Calendar.getInstance().getTime()), //When was it placed
                event.player.getPersistentID(), // Who placed it (UUID)
                event.world.provider.getDimensionId(), // What dimension was it in
                event.pos.getX(), // XCoord
                event.pos.getY(), // YCoord
                event.pos.getZ(), // ZCoord
                event.state.getBlock().getUnlocalizedName() // What block was it

        ) + Reference.LINE_SEPARATOR) {
            @Override
            public void run() {
                placeFileLock.unlock();
                try {
                    if (!logFile.exists()) {
                        writeHeader(logFile);
                    }
                    Files.append(data, logFile, Reference.CHARSET);
                } catch (IOException e) {
                    super.log.warn("Failed to save block place file to disk", e);
                } finally {
                    placeFileLock.unlock();
                }
            }
        }.start();
    }

    private static void writeHeader(File file) throws IOException {
        Files.append(FILE_HEADER + Reference.LINE_SEPARATOR, file, Reference.CHARSET);
    }
}
