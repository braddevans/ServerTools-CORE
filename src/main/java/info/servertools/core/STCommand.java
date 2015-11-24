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

import info.servertools.core.util.ServerUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class STCommand extends CommandBase {

    public static final int PERMISSION_EVERYONE = 0;
    public static final int PERMISSION_BYPASS_SPAWN = 1;
    public static final int PERMISSION_OPERATOR = 2;
    public static final int PERMISSION_ADMIN = 3;
    public static final int PERMISSION_SUPERADMIN = 4;

    private final String defaultName;
    private String name;
    private int permissionLevel = PERMISSION_SUPERADMIN;

    public STCommand(final String defaultName) {
        this.defaultName = requireNonNull(defaultName, "defaultName");
    }

    public String getDefaultName() {
        return this.defaultName;
    }

    @Override
    public final String getCommandName() {
        return this.name;
    }

    /**
     * <em>Internal Use Only!</em>
     *
     * @param name Name
     */
    void setName(final String name) {
        this.name = name;
    }

    @Override
    public final int getRequiredPermissionLevel() {
        return this.permissionLevel;
    }

    /**
     * <em>Internal use only!</em>
     *
     * @param permissionLevel The permission level
     */
    protected void setPermissionLevel(final int permissionLevel) {
        this.permissionLevel = permissionLevel;
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
                "defaultName='" + defaultName + '\'' +
                ", name='" + name + '\'' +
                ", permissionLevel=" + permissionLevel +
                '}';
    }

/* ---------- Utilities ---------- */

    public static EntityPlayerMP requirePlayer(final ICommandSender sender) throws CommandException {
        return requirePlayer(sender, "That command can only be used by a player");
    }

    public static EntityPlayerMP requirePlayer(final ICommandSender sender, final String message) throws CommandException {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(message, "message");
        if (sender instanceof EntityPlayerMP) {
            return (EntityPlayerMP) sender;
        } else {
            throw new CommandException(message);
        }
    }
}
