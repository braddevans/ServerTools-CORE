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
package info.servertools.core.command;

import info.servertools.core.util.ServerUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.mojang.authlib.GameProfile;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class CommandUUID extends STCommand {

    public CommandUUID() {
        super("uuid");
        setPermissionLevel(PERMISSION_EVERYONE);
    }

    @Override
    public String getCommandUsage(final ICommandSender sender) {
        return "/" + getCommandName() + " [username]";
    }

    @Override
    @Nullable
    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        return args.length <= 1 ? getListOfStringsMatchingLastWord(args, ServerUtils.getAllUsernames()) : null;
    }

    @Override
    public boolean isUsernameIndex(final String[] args, final int index) {
        return index == 0;
    }

    @Override
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        GameProfile gameProfile;
        if (args.length == 1) {
            Optional<GameProfile> optGameProfile = ServerUtils.getGameProfile(args[0]);
            if (optGameProfile.isPresent()) {
                gameProfile = optGameProfile.get();
            } else {
                throw new PlayerNotFoundException();
            }
        } else if (args.length == 0) {
            gameProfile = getCommandSenderAsPlayer(sender).getGameProfile();
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        ChatComponentText text = new ChatComponentText(gameProfile.getName() + "'s UUID is: ");
        ChatComponentText uuidText = new ChatComponentText(gameProfile.getId().toString());
        uuidText.getChatStyle().setColor(EnumChatFormatting.AQUA);
        text.appendSibling(uuidText);
        sender.addChatMessage(text);
    }
}
