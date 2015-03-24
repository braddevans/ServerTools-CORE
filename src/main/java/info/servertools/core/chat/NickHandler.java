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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import info.servertools.core.ServerTools;
import info.servertools.core.config.CoreConfig;
import info.servertools.core.lib.Reference;
import info.servertools.core.util.ChatMessage;
import info.servertools.core.util.ServerUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.minecraft.util.EnumChatFormatting.AQUA;
import static net.minecraft.util.EnumChatFormatting.WHITE;

public class NickHandler {

    private static final Logger log = LogManager.getLogger();

    private static final Type type = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{UUID.class, String.class};
        }

        @Override
        public Type getRawType() {
            return ConcurrentHashMap.class;
        }

        @Nullable
        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    private Map<UUID, String> nickMap = new ConcurrentHashMap<>();

    private Path saveFile;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public NickHandler(final Path saveFile) {
        this.saveFile = checkNotNull(saveFile);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void load() {
        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (Files.exists(saveFile)) {
                    try (BufferedReader reader = Files.newBufferedReader(saveFile, Reference.CHARSET)) {
                        nickMap = gson.fromJson(reader, type);
                    } catch (IOException e) {
                        log.error("Failed to read nicknames from file " + saveFile, e);
                    }
                }
            }
        });
    }

    public void save() {
        ServerTools.executorService.execute(new Runnable() {
            @Override
            public void run() {
                try (BufferedWriter writer = Files.newBufferedWriter(saveFile, Reference.CHARSET)) {
                    gson.toJson(nickMap, type, writer);
                } catch (IOException e) {
                    log.error("Failed to save nicknames to file " + saveFile, e);
                }
            }
        });
    }

    @SubscribeEvent
    public void nameFormat(PlayerEvent.NameFormat event) {
        if (nickMap.containsKey(event.entityPlayer.getPersistentID())) {
            String nick = nickMap.get(event.entityPlayer.getPersistentID());
            event.displayname = event.displayname.replace(event.entityPlayer.getGameProfile().getName(), nick);
        }
    }

    @Nullable
    public String getNick(final UUID uuid) {
        return nickMap.get(uuid);
    }

    public boolean setNick(EntityPlayerMP player, String nick) {
        if (!verifyNickname(player, nick)) {
            return false;
        }
        nickMap.put(player.getPersistentID(), nick);
        player.refreshDisplayName();
        save();

        if (CoreConfig.BROADCAST_NICK_CHANGES) {
            MinecraftServer.getServer().getConfigurationManager().sendChatMsg(ChatMessage.builder()
                    .color(AQUA).add(player.getGameProfile().getName())
                    .color(WHITE).add(" is now known as ")
                    .color(AQUA).add(nick)
                    .build());
        }

        return true;
    }

    /**
     * Verify that the supplied nickname is valid
     *
     * @param nickName The nickname
     * @return {@code true} if the nickname is valid, {@code false} if not
     */
    public static boolean verifyNickname(final EntityPlayerMP player, final String nickName) {
        checkNotNull(nickName, "nickName");
        for (final EntityPlayerMP playerMP : ServerUtils.getAllPlayers()) {
            if (playerMP == player) {
                continue;
            }
            if (nickName.equalsIgnoreCase(playerMP.getCommandSenderName())) {
                return false;
            }
        }

        for (final Pattern pattern : CoreConfig.NICKNAME_BLACKLIST) {
            if (pattern.matcher(nickName).matches()) {
                return false;
            }
        }

        return true;
    }
}
