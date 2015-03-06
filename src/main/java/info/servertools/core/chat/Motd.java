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

import info.servertools.core.ServerTools;
import info.servertools.core.config.CoreConfig;
import info.servertools.core.lib.Reference;
import info.servertools.core.util.ChatUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private final Path motdFile;
    private final Lock fileLock = new ReentrantLock();

    private final Collection<String> motd = new ArrayList<>();
    private final ReadWriteLock motdLock = new ReentrantReadWriteLock();

    public Motd(final Path motdFile) {
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
        if (!Files.exists(motdFile)) {
            genDefaultMotd();
            motdLock.writeLock().lock();
            Collections.addAll(motd, MOTD_DEFAULT);
            motdLock.writeLock().unlock();
        } else {
            ServerTools.executorService.execute(new Runnable() {
                @Override
                public void run() {
                    fileLock.lock();
                    motdLock.writeLock().lock();
                    motd.clear();
                    try (BufferedReader reader = Files.newBufferedReader(motdFile, Reference.CHARSET)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            motd.add(line);
                        }
                    } catch (IOException e) {
                        log.error("Failed to read MOTD from file: " + motdFile, e);
                    } finally {
                        fileLock.unlock();
                        motdLock.writeLock().unlock();
                    }
                }
            });
        }
    }

    /**
     * Generate the default MOTD and save it to disk
     */
    public void genDefaultMotd() {
        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                fileLock.lock();
                try (BufferedWriter writer = Files.newBufferedWriter(motdFile, Reference.CHARSET)) {
                    Files.delete(motdFile);
                    for (final String line : MOTD_DEFAULT) {
                        writer.write(line + Reference.LINE_SEPARATOR);
                    }
                } catch (IOException e) {
                    log.error("Failed to generate the default MOTD to file: " + motdFile, e);
                } finally {
                    fileLock.unlock();
                }
            }
        });
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (CoreConfig.ENABLE_MOTD_ON_LOGIN) {
            serveMotd(event.player);
        }
    }
}
