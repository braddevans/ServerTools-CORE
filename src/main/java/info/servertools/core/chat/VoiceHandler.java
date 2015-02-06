/*
 * Copyright 2014 ServerTools
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

import static net.minecraft.util.EnumChatFormatting.BLUE;
import static net.minecraft.util.EnumChatFormatting.RED;
import static net.minecraft.util.EnumChatFormatting.RESET;

import info.servertools.core.CoreConfig;
import info.servertools.core.ServerTools;
import info.servertools.core.lib.Reference;
import info.servertools.core.lib.Strings;
import info.servertools.core.util.ChatUtils;
import info.servertools.core.util.ServerUtils;

import net.minecraft.command.server.CommandBroadcast;
import net.minecraft.command.server.CommandEmote;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.command.server.CommandMessageRaw;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VoiceHandler {

    private static final Logger log = LogManager.getLogger(VoiceHandler.class);

    private final File voiceFile = new File(ServerTools.serverToolsDir, "voice.json");
    private final File silenceFile = new File(ServerTools.serverToolsDir, "silence.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Set<UUID> voicedUsers = new HashSet<>();
    private Set<UUID> silencedUsers = new HashSet<>();

    public VoiceHandler() {
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
    public boolean isUserVoiced(UUID uuid) {
        return voicedUsers.contains(uuid);
    }

    /**
     * Give voice to a player.
     *
     * @param uuid The player's {@link UUID}
     *
     * @return {@code true} if the entry was added, {@code false} if it already existed
     */
    public boolean addVoice(UUID uuid) {
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
    public boolean isUserSilenced(UUID uuid) {
        return silencedUsers.contains(uuid);
    }

    /**
     * Set a silence on a player.
     *
     * @param uuid The player's {@link UUID}
     *
     * @return {@code true} if the entry was added, {@code false} if it already existed
     */
    public boolean addSilence(UUID uuid) {
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
     * Get an {@link ImmutableSet immutable} set of the voiced users.
     *
     * @return A set of voiced users
     */
    public ImmutableSet<UUID> getVoicedUsers() {
        return ImmutableSet.copyOf(voicedUsers);
    }

    /**
     * Get an {@link ImmutableSet immutable} set of the silenced users.
     *
     * @return A set of voiced users
     */
    public ImmutableSet<UUID> getSilencedUsers() {
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
            voicedUsers = gson.fromJson(Files.toString(voiceFile, Reference.CHARSET), new TypeToken<Set<UUID>>() {}.getType());
        } catch (Exception e) {
            log.error("Failed to load voiced players from file " + voiceFile, e);
        } finally {
            if (voicedUsers == null) { voicedUsers = Sets.newHashSet(); }
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
            silencedUsers = gson.fromJson(Files.toString(silenceFile, Reference.CHARSET), new TypeToken<Set<UUID>>() {}.getType());
        } catch (Exception e) {
            log.error("Failed to load silenced players from file " + silenceFile, e);
        }
    }

    @SubscribeEvent
    public void nameFormat(PlayerEvent.NameFormat event) {
        if (CoreConfig.ENABLE_OP_PREFIX) {
            if (!MinecraftServer.getServer().isSinglePlayer() && ServerUtils.isOP(event.entityPlayer.getGameProfile())) {
                event.displayname = String.valueOf(RED) + '[' + CoreConfig.OP_CHAT_PREFIX + "] " + RESET + event.displayname;
            }
        }

        if (isUserVoiced(event.entityPlayer.getPersistentID())) {
            event.displayname = String.valueOf(BLUE) + "[" + CoreConfig.VOICE_CHAT_PREFIX + "] " + RESET + event.displayname;
        }
    }

    @SubscribeEvent
    public void serverChat(ServerChatEvent event) {
        if (isUserSilenced(event.player.getPersistentID())) {
            event.setCanceled(true);
            event.player.addChatComponentMessage(ChatUtils.getChatComponent(Strings.ERROR_SILENCED, EnumChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public void command(CommandEvent event) {
        if (event.sender instanceof EntityPlayerMP && isUserSilenced(((EntityPlayerMP) event.sender).getPersistentID())) {
            if (event.command instanceof CommandBroadcast
                || event.command instanceof CommandMessage
                || event.command instanceof CommandEmote
                || event.command instanceof CommandMessageRaw) {

                event.setCanceled(true);
                event.sender.addChatMessage(ChatUtils.getChatComponent(Strings.ERROR_SILENCED, EnumChatFormatting.RED));
            }
        }
    }
}
