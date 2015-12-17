/*
 * This file is a part of ServerTools <http://servertools.info>
 *
 * Copyright (c) 2015 ServerTools
 * Copyright (c) 2015 contributors
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

import static java.util.Objects.requireNonNull;

import info.servertools.core.util.ServerUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Base class for all ServerTools commands. A user-facing configuration file can be edited to rename, disable, and change required permission level
 * for any command that implements this.
 * <p>
 * <b>Do not</b> register these commands with Minecraft yourself. Instead annotate them with {@link Command} and they will be auto-registered.
 * </p>
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public abstract class STCommand extends CommandBase {

    public static final int PERMISSION_EVERYONE = 0;
    public static final int PERMISSION_BYPASS_SPAWN = 1;
    public static final int PERMISSION_OPERATOR = 2;
    public static final int PERMISSION_ADMIN = 3;
    public static final int PERMISSION_SUPERADMIN = 4;

    private String name; // Will be reflectively set
    private int permissionLevel; // Will be reflectively set

    /**
     * Get the registered name of the command. This can be changed via a configuraion file
     *
     * @return The registered name
     */
    @Override
    public final String getCommandName() {
        return this.name;
    }

    @Override
    public final int getRequiredPermissionLevel() {
        return this.permissionLevel;
    }

    @Override
    public boolean canCommandSenderUseCommand(final ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            final EntityPlayerMP player = ((EntityPlayerMP) sender);
            final int permLevel = getRequiredPermissionLevel();
            return permLevel == 0 || ServerUtils.isEffectiveOp(player.getGameProfile());
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "STCommand{" +
                ", name='" + name + '\'' +
                ", permissionLevel=" + permissionLevel +
                '}';
    }

/* ---------- Utilities ---------- */

    /**
     * Requre that a {@linkplain ICommandSender command sender} instance be an instance of {@linkplain EntityPlayerMP}
     *
     * @param sender The command sender
     *
     * @return The {@code sender} cast to an {@linkplain EntityPlayerMP}
     *
     * @throws CommandException If the {@code sender} was not an instance of {@linkplain EntityPlayerMP}
     */
    public static EntityPlayerMP requirePlayer(final ICommandSender sender) throws CommandException {
        return requirePlayer(sender, "That command can only be used by a player");
    }

    /**
     * Requre that a {@linkplain ICommandSender command sender} instance be an instance of {@linkplain EntityPlayerMP}
     *
     * @param sender  The command sender
     * @param message The message to populate the {@linkplain CommandException} with
     *
     * @return The {@code sender} cast to an {@linkplain EntityPlayerMP}
     *
     * @throws CommandException If the {@code sender} was not an instance of {@linkplain EntityPlayerMP}
     */
    public static EntityPlayerMP requirePlayer(final ICommandSender sender, final String message) throws CommandException {
        requireNonNull(sender, "sender");
        requireNonNull(message, "message");
        if (sender instanceof EntityPlayerMP) {
            return (EntityPlayerMP) sender;
        } else {
            throw new CommandException(message);
        }
    }
}
