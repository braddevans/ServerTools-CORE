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
package info.servertools.core.command.corecommands;

import info.servertools.core.command.CommandLevel;
import info.servertools.core.command.ServerToolsCommand;
import info.servertools.core.lib.Strings;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class CommandInventory extends ServerToolsCommand {

    public CommandInventory(String defaultName) {
        super(defaultName);
    }

    @Override
    public CommandLevel getCommandLevel() {

        return CommandLevel.OP;
    }

    @Override
    public List getAliases() {

        return Collections.singletonList("inv");
    }

    @Nullable
    @Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr, BlockPos pos) {
        return par2ArrayOfStr.length >= 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames()) : null;
    }

    @Override
    public boolean isUsernameIndex(String[] par1ArrayOfStr, int par2) {

        return par2 == 0;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {

        return "/" + name + " {username}";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] astring) throws CommandException {

        if (!(sender instanceof EntityPlayerMP)) { throw new WrongUsageException(Strings.COMMAND_ERROR_ONLYPLAYER); }

        EntityPlayerMP player;

        if (astring.length == 0) { player = (EntityPlayerMP) sender; } else { player = getPlayer(sender, astring[0]); }

        ((EntityPlayerMP) sender).displayGUIChest(new InvPlayerWrapper((EntityPlayerMP) sender, player));
    }

    public static class InvPlayerWrapper implements IInventory {

        private final EntityPlayerMP viewer;
        private final EntityPlayer player;

        public InvPlayerWrapper(EntityPlayerMP viewer, EntityPlayer player) {
            this.viewer = viewer;
            this.player = player;
        }

        @Override
        public int getSizeInventory() {
            if (player.isDead) {
                viewer.closeScreen();
            }

            return 45;
        }

        @Nullable
        @Override
        public ItemStack getStackInSlot(int var1) {
            if (player.isDead) {
                viewer.closeScreen();
                return null;
            }

            if (var1 >= 0 && var1 < 27) {
                return player.inventory.mainInventory[var1 + 9];
            } else if (var1 >= 27 && var1 < 36) {
                return player.inventory.mainInventory[var1 - 27];
            } else if (var1 >= 36 && var1 < 40) {
                return player.inventory.armorInventory[39 - var1];
            } else { return null; }
        }

        @Nullable
        @Override
        public ItemStack decrStackSize(int i, int j) {
            if (player.isDead) {
                viewer.closeScreen();
                return null;
            }

            @Nullable ItemStack stack = getStackInSlot(i);
            if (stack != null) {
                if (stack.stackSize <= j) {
                    setInventorySlotContents(i, null);
                    markDirty();
                    return stack;
                }
                ItemStack stack1 = stack.splitStack(j);
                if (stack.stackSize == 0) {
                    setInventorySlotContents(i, null);
                }
                markDirty();
                return stack1;
            } else { return null; }
        }

        @Nullable
        @Override
        public ItemStack getStackInSlotOnClosing(int var1) {
            if (player.isDead) {
                viewer.closeScreen();
                return null;
            }

            @Nullable ItemStack stack = getStackInSlot(var1);
            if (stack != null) {
                setInventorySlotContents(var1, null);
                return stack;
            } else { return null; }
        }

        @Override
        public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
            if (player.isDead) {
                if (var2 != null) {
                    viewer.entityDropItem(var2, 0.5F);
                }
                viewer.closeScreen();
                return;
            }

            if (var1 >= 0 && var1 < 27) {
                player.inventory.mainInventory[var1 + 9] = var2;
            } else if (var1 >= 27 && var1 < 36) {
                player.inventory.mainInventory[var1 - 27] = var2;
            } else if (var1 >= 36 && var1 < 40) {
                player.inventory.armorInventory[39 - var1] = var2;
            } else if (var2 != null) {
                viewer.entityDropItem(var2, 0.5F);
            }
        }

        @Override
        public String getName() {
            return player.getName();
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Nullable
        @Override
        public IChatComponent getDisplayName() {
            return null;
        }

        @Override
        public int getInventoryStackLimit() {
            if (player.isDead) {
                viewer.closeScreen();
                return 64;
            }
            return player.inventory.getInventoryStackLimit();
        }

        @Override
        public void markDirty() {}

        @Override
        public boolean isUseableByPlayer(EntityPlayer var1) {
            if (player.isDead) {
                viewer.closeScreen();
                return false;
            }
            return true;
        }

        @Override
        public void openInventory(EntityPlayer player) {}

        @Override
        public void closeInventory(EntityPlayer player) {}

        @Override
        public boolean isItemValidForSlot(int i, ItemStack itemstack) {
            return true;
        }

        @Override
        public int getField(int id) {
            return 0;
        }

        @Override
        public void setField(int id, int value) {}

        @Override
        public int getFieldCount() {
            return 0;
        }

        @Override
        public void clear() {}
    }
}
