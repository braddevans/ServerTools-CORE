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
package info.servertools.core.chat;

import static com.google.common.base.Preconditions.checkNotNull;

import info.servertools.core.config.STConfig;
import info.servertools.core.lib.Reference;
import info.servertools.core.util.ChatUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.io.Files;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Motd {

    public static final String[] MOTD_DEFAULT = new String[]{
            "Hello, $AQUA$$PLAYER$!",
            "This is the default MOTD. In order to change it,",
            "edit the motd.txt in the servertools directory"
    };

    private static final Logger log = LogManager.getLogger(Motd.class);

    private final File motdFile;
    private final Lock fileLock = new ReentrantLock();

    private final Collection<String> motd = new ArrayList<>();
    private final ReadWriteLock motdLock = new ReentrantReadWriteLock();

    public Motd(final File motdFile) {
        this.motdFile = checkNotNull(motdFile, "motdFile");
        FMLCommonHandler.instance().bus().register(this);
        loadMotd();
    }

    /**
     * Serve the current {@link #motd} to the given player. Wildcards will be expanded relative to the player
     *
     * @param player The player
     */
    public void serveMotd(final EntityPlayer player) {
        motdLock.readLock().lock();
        for (String line : motd) {
            line = line.replace("$PLAYER$", player.getDisplayNameString());
            for (final EnumChatFormatting formatting : EnumChatFormatting.values()) {
                line = line.replace('$' + formatting.getFriendlyName().toUpperCase() + '$', formatting.toString());
            }
            player.addChatComponentMessage(ChatUtils.getChatComponent(line));
        }
        motdLock.readLock().unlock();
    }

    /**
     * Load the MOTD from disk. If the file doesn't exist, the default will be created
     *
     * @see #genDefaultMotd()
     */
    public void loadMotd() {
        if (!motdFile.exists()) {
            genDefaultMotd();
            motdLock.writeLock().lock();
            Collections.addAll(motd, MOTD_DEFAULT);
            motdLock.writeLock().unlock();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    motdLock.writeLock().lock();
                    try {
                        motd.clear();
                        motd.addAll(Files.readLines(motdFile, Reference.CHARSET));
                    } catch (IOException e) {
                        log.error("Failed to read MOTD from file: " + motdFile, e);
                    } finally {
                        motdLock.writeLock().unlock();
                    }
                }
            }).start();
        }
    }

    /**
     * Generate the default MOTD and save it to disk
     */
    public void genDefaultMotd() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fileLock.lock();
                try {
                    motdFile.delete();
                    for (final String line : MOTD_DEFAULT) {
                        Files.append(line + Reference.LINE_SEPARATOR, motdFile, Reference.CHARSET);
                    }
                } catch (IOException e) {
                    log.error("Failed to generate the default MOTD to file: " + motdFile, e);
                } finally {
                    fileLock.unlock();
                }
            }
        }).start();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (STConfig.settings().ENABLE_MOTD_LOGIN) {
            serveMotd(event.player);
        }
    }
}
