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
import static net.minecraft.util.EnumChatFormatting.RESET;

import info.servertools.core.config.STConfig;
import info.servertools.core.lib.Reference;
import info.servertools.core.util.ChatUtils;
import info.servertools.core.util.ServerUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gnu.trove.set.hash.THashSet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

public class VoiceSilenceHandler {

    private static final Logger log = LogManager.getLogger(VoiceSilenceHandler.class);

    private final File voiceFile;
    private final File silenceFile;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type setType = new TypeToken<THashSet<UUID>>() {}.getType();

    private final THashSet<UUID> voicedUsers = new THashSet<>();
    private final THashSet<UUID> silencedUsers = new THashSet<>();

    public VoiceSilenceHandler(File voiceFile, File silenceFile) {
        this.voiceFile = checkNotNull(voiceFile, "voiceFile");
        this.silenceFile = checkNotNull(silenceFile, "silenceFile");
        loadSilenceList();
        loadVoiceList();
        MinecraftForge.EVENT_BUS.register(this);
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
            ServerUtils.refreshPlayerDisplayName(uuid);
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
            ServerUtils.refreshPlayerDisplayName(uuid);
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
        final String json = gson.toJson(voicedUsers);
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (voiceFile) {
                    try {
                        Files.write(json, voiceFile, Reference.CHARSET);
                    } catch (IOException e) {
                        log.error("Failed to save voice file " + voiceFile + " to disk", e);
                    }
                }
            }
        }).start();
    }

    /**
     * Load the voiced users from disk. <b>This is done on the main thread!</b>
     */
    public void loadVoiceList() {
        if (!voiceFile.exists()) { return; }
        try {
            final String json = Files.toString(voiceFile, Reference.CHARSET);
            @Nullable final Set<UUID> set = gson.fromJson(json, setType);
            if (set != null) {
                voicedUsers.clear();
                voicedUsers.addAll(set);
            }
        } catch (IOException e) {
            log.error("Failed to load voiced players from file " + voiceFile, e);
        }
    }

    /**
     * Save the silenced users to disk. This is done in a separate thread.
     */
    public void saveSilenceList() {
        final String json = gson.toJson(silencedUsers);
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (silenceFile) {
                    try {
                        Files.write(json, silenceFile, Reference.CHARSET);
                    } catch (IOException e) {
                        log.error("Failed to save silence file " + silenceFile + " to disk", e);
                    }
                }
            }
        }).start();
    }

    /**
     * Load the silenced users from disk. <b>This is done on the main thread!</b>
     */
    public void loadSilenceList() {
        if (!silenceFile.exists()) { return; }
        try {
            final String json = Files.toString(silenceFile, Reference.CHARSET);
            @Nullable final Set<UUID> set = gson.fromJson(json, setType);
            if (set != null) {
                silencedUsers.clear();
                silencedUsers.addAll(set);
            }
        } catch (IOException e) {
            log.error("Failed to load silenced players from file " + silenceFile, e);
        }
    }

    @SubscribeEvent
    public void nameFormat(PlayerEvent.NameFormat event) {
        if (STConfig.settings().ENABLE_OP_PREFIX) {
            if (!MinecraftServer.getServer().isSinglePlayer() && ServerUtils.isOP(event.entityPlayer.getGameProfile())) {
                event.displayname = String.valueOf(RED) + '[' + STConfig.settings().OP_PREFIX + "] " + RESET + event.displayname;
            }
        }

        if (isPlayerVoiced(event.entityPlayer.getPersistentID())) {
            event.displayname = String.valueOf(BLUE) + "[" + STConfig.settings().VOICE_PREFIX + "] " + RESET + event.displayname;
        }
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
            STConfig.settings().SILENCE_BLACKLISTED_COMMANDS.contains(event.command.getCommandName())) {

            event.setCanceled(true);
            event.sender.addChatMessage(ChatUtils.getChatComponent("You are silenced on this server", EnumChatFormatting.RED));
        }
    }
}
