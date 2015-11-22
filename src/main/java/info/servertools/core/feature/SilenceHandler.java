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

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import info.servertools.core.Constants;
import info.servertools.core.ServerToolsCore;
import info.servertools.core.util.FileIO;
import net.minecraft.command.server.CommandBroadcast;
import net.minecraft.command.server.CommandEmote;
import net.minecraft.command.server.CommandMessage;
import net.minecraft.command.server.CommandMessageRaw;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.CREATE;

public class SilenceHandler {

    private static final Logger log = LogManager.getLogger();

    private final Set<String> bannedCommands = Sets.newHashSet(
            CommandEmote.class.getName(),
            CommandMessage.class.getName(),
            CommandMessageRaw.class.getName(),
            CommandBroadcast.class.getName()
    );

    private final Path saveFile;

    private Set<UUID> silencedUsers = new HashSet<>();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Type type = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() { return new Type[]{UUID.class}; }

        @Override
        public Type getRawType() { return HashSet.class; }

        @Nullable
        @Override
        public Type getOwnerType() { return null; }
    };

    public SilenceHandler(final Path saveFile) throws IOException {
        this.saveFile = saveFile;
        bannedCommands.addAll(ServerToolsCore.getConfig().getChat().getAdditionalSilenceCommands());
        MinecraftForge.EVENT_BUS.register(this);
        load();
    }

    /**
     * Add a player to the silence list
     *
     * @param uuid The player's UUID
     *
     * @return {@code true} if the player was silenced, {@code false} if the player was already silenced
     */
    public boolean addSilence(final UUID uuid) {
        if (silencedUsers.add(uuid)) {
            save();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove a player from the silence list
     *
     * @param uuid The player's UUID
     *
     * @return {@code true} if the player was un-silenced, {@code false} if the player was not silenced
     */
    public boolean removeSilence(final UUID uuid) {
        if (silencedUsers.remove(uuid)) {
            save();
            return true;
        } else {
            return false;
        }
    }

    private void save() {
        FileIO.submitTask(() -> {
            synchronized (saveFile) {
                try {
                    if (!Files.exists(saveFile.getParent())) {
                        Files.createDirectories(saveFile.getParent());
                    }
                    Files.deleteIfExists(saveFile);
                    try (BufferedWriter writer = Files.newBufferedWriter(saveFile, Constants.CHARSET, CREATE)) {
                        gson.toJson(silencedUsers, type, writer);
                    }
                } catch (IOException e) {
                    log.error("Failed to save silence file {}", saveFile, e);
                }
            }
        });
    }

    private void load() throws IOException {
        synchronized (saveFile) {
            if (!Files.exists(saveFile)) return;
            try (BufferedReader reader = Files.newBufferedReader(saveFile, Constants.CHARSET)) {
                this.silencedUsers = gson.fromJson(reader, type);
                if (this.silencedUsers == null) this.silencedUsers = new HashSet<>();
            } catch (IOException e) {
                log.error("Failed to laod silence file {}", saveFile);
                throw e;
            }
        }
    }

    @SubscribeEvent
    public void onServerChat(final ServerChatEvent event) {
        if (silencedUsers.contains(event.player.getPersistentID())) {
            ChatComponentText text = new ChatComponentText("You are silenced from chat");
            text.getChatStyle().setColor(EnumChatFormatting.RED);
            event.player.addChatMessage(text);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onCommand(final CommandEvent event) {
        if (bannedCommands.contains(event.command.getClass().getName())) {
            if (event.sender instanceof EntityPlayerMP) {
                final EntityPlayerMP player = (EntityPlayerMP) event.sender;
                if (silencedUsers.contains(player.getPersistentID())) {
                    ChatComponentText text = new ChatComponentText("You are silenced from chat");
                    text.getChatStyle().setColor(EnumChatFormatting.RED);
                    player.addChatMessage(text);
                    event.setCanceled(true);
                }
            }
        }
    }
}
