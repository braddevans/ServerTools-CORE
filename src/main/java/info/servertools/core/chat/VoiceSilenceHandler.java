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
import static net.minecraft.util.EnumChatFormatting.BLUE;
import static net.minecraft.util.EnumChatFormatting.RED;

import info.servertools.core.config.CoreConfig;
import info.servertools.core.lib.Reference;
import info.servertools.core.util.ChatUtils;
import info.servertools.core.util.ServerUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

public class VoiceSilenceHandler {

    private static final Logger log = LogManager.getLogger(VoiceSilenceHandler.class);

    private static final Type type = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() { return new Type[]{UUID.class}; }

        @Override
        public Type getRawType() { return Sets.newConcurrentHashSet().getClass(); }

        @Nullable
        @Override
        public Type getOwnerType() { return null; }
    };

    private final Path voiceFile;
    private final Lock voiceFileLock = new ReentrantLock();

    private final Path silenceFile;
    private final Lock silenceFileLock = new ReentrantLock();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Set<UUID> voicedUsers = Sets.newConcurrentHashSet();
    private Set<UUID> silencedUsers = Sets.newConcurrentHashSet();

    final IChatComponent opPrefix = new ChatComponentText('[' + CoreConfig.OP_PREFIX + "] ");
    final IChatComponent voicePrefix = new ChatComponentText('[' + CoreConfig.VOICE_PREFIX + "] ");

    public VoiceSilenceHandler(Path voiceFile, Path silenceFile) {
        this.voiceFile = checkNotNull(voiceFile, "voiceFile");
        this.silenceFile = checkNotNull(silenceFile, "silenceFile");
        loadSilenceList();
        loadVoiceList();
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        opPrefix.getChatStyle().setColor(RED);
        voicePrefix.getChatStyle().setColor(BLUE);
    }

    /**
     * Check if a player has voice.
     *
     * @param uuid The player's {@link UUID}
     *
     * @return {@code true} if the player is voiced, {@code false} if not
     */
    public boolean isPlayerVoiced(UUID uuid) {
        return voicedUsers.contains(uuid);
    }

    /**
     * Give voice to a player.
     *
     * @param uuid The player's {@link UUID}
     *
     * @return {@code true} if the entry was added, {@code false} if it already existed
     */
    public boolean giveVoice(UUID uuid) {
        if (voicedUsers.add(uuid)) {
            saveVoiceList();
            @Nullable EntityPlayer player = ServerUtils.getPlayerForUUID(uuid);
            if (player != null) {
                refreshTags(player);
            }
            return true;
        }
        return false;
    }

    /**
     * Remove voice from a player.
     *
     * @param uuid The player's {@link UUID}
     *
     * @return {@code true} if the entry was removed, {@code false} if it didn't exist
     */
    public boolean removeVoice(UUID uuid) {
        if (voicedUsers.contains(uuid)) {
            voicedUsers.remove(uuid);
            saveVoiceList();
            @Nullable EntityPlayer player = ServerUtils.getPlayerForUUID(uuid);
            if (player != null) {
                refreshTags(player);
            }
            return true;
        }
        return false;
    }

    /**
     * Check if a player is silenced.
     *
     * @param uuid The player's {@link UUID}
     *
     * @return {@code true} if the player is silenced, {@code false} if not
     */
    public boolean isPlayerSilenced(UUID uuid) {
        return silencedUsers.contains(uuid);
    }

    /**
     * Set a silence on a player.
     *
     * @param uuid The player's {@link UUID}
     *
     * @return {@code true} if the entry was added, {@code false} if it already existed
     */
    public boolean setSilence(UUID uuid) {
        if (silencedUsers.add(uuid)) {
            saveSilenceList();
            return true;
        }
        return false;
    }

    /**
     * Remove silence on a player.
     *
     * @param uuid The player's {@link UUID}
     *
     * @return {@code true} if the entry was removed, {@code false} if it didn't exist
     */
    public boolean removeSilence(UUID uuid) {
        if (silencedUsers.contains(uuid)) {
            silencedUsers.remove(uuid);
            saveSilenceList();
            return true;
        }
        return false;
    }

    /**
     * Get an {@link ImmutableSet immutable} set of the voiced players.
     *
     * @return A set of voiced players
     */
    public ImmutableSet<UUID> getVoicedPlayers() {
        return ImmutableSet.copyOf(voicedUsers);
    }

    /**
     * Get an {@link ImmutableSet immutable} set of the silenced players.
     *
     * @return A set of voiced players
     */
    public ImmutableSet<UUID> getSilencedPlayers() {
        return ImmutableSet.copyOf(silencedUsers);
    }

    /**
     * Save the voiced users to disk. This is done in a separate thread.
     */
    public void saveVoiceList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                voiceFileLock.lock();
                try (BufferedWriter writer = Files.newBufferedWriter(voiceFile, Reference.CHARSET)) {
                    gson.toJson(voicedUsers, type, writer);
                } catch (IOException e) {
                    log.error("Failed to save voice file " + voiceFile + " to disk", e);
                } finally {
                    voiceFileLock.unlock();
                }
            }
        }).start();
    }

    /**
     * Load the voiced users from disk. <b>This is done on the main thread!</b>
     */
    public void loadVoiceList() {
        if (!Files.exists(voiceFile)) {
            return;
        }
        voiceFileLock.lock();
        try (BufferedReader reader = Files.newBufferedReader(voiceFile, Reference.CHARSET)) {
            voicedUsers = gson.fromJson(reader, type);
        } catch (IOException e) {
            log.error("Failed to load voiced players from file " + voiceFile, e);
        } finally {
            voiceFileLock.unlock();
        }
    }

    /**
     * Save the silenced users to disk. This is done in a separate thread.
     */
    public void saveSilenceList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                silenceFileLock.lock();
                try (BufferedWriter writer = Files.newBufferedWriter(silenceFile, Reference.CHARSET)) {
                    gson.toJson(silencedUsers, type, writer);
                } catch (IOException e) {
                    log.error("Failed to save silence file " + silenceFile + " to disk", e);
                } finally {
                    silenceFileLock.unlock();
                }
            }
        }).start();
    }

    /**
     * Load the silenced users from disk. <b>This is done on the main thread!</b>
     */
    public void loadSilenceList() {
        if (!Files.exists(silenceFile)) {
            return;
        }
        silenceFileLock.lock();
        try (BufferedReader reader = Files.newBufferedReader(silenceFile, Reference.CHARSET)) {
            silencedUsers = gson.fromJson(reader, type);
        } catch (IOException e) {
            log.error("Failed to load silenced players from file " + silenceFile, e);
        } finally {
            silenceFileLock.unlock();
        }
    }

    public void refreshTags(EntityPlayer player) {
        if (isPlayerVoiced(player.getPersistentID())) {
            if (!player.getPrefixes().contains(voicePrefix)) {
                player.addPrefix(voicePrefix);
            }
        } else {
            player.getPrefixes().remove(voicePrefix);
        }
    }

    @SubscribeEvent
    public void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        refreshTags(event.player);
    }

    @SubscribeEvent
    public void serverChat(ServerChatEvent event) {
        if (isPlayerSilenced(event.player.getPersistentID())) {
            event.setCanceled(true);
            event.player.addChatComponentMessage(ChatUtils.getChatComponent("You are silenced on this server", EnumChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public void command(CommandEvent event) {

        if (event.sender instanceof EntityPlayerMP &&
            isPlayerSilenced(((EntityPlayerMP) event.sender).getPersistentID()) &&
            CoreConfig.SILENCE_BLACKLISTED_COMMANDS.contains(event.command.getCommandName())) {

            event.setCanceled(true);
            event.sender.addChatMessage(ChatUtils.getChatComponent("You are silenced on this server", EnumChatFormatting.RED));
        }
    }
}
