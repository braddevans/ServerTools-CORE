package info.servertools.core.teleport;

import info.servertools.core.ServerTools;
import info.servertools.core.util.Location;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

public class BackHandler {

    private final Map<UUID, Location> backMap = new HashMap<>();

    public BackHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Get a player's back location without removing it
     *
     * @param uuid The player's {@link Entity#getPersistentID() UUID}
     *
     * @return The back location or {@code null} if one doesn't exist
     */
    @Nullable
    public Location peekBackLocation(final UUID uuid) {
        return backMap.get(uuid);
    }

    /**
     * Get a player's back location and remove it
     *
     * @param uuid The player's {@link Entity#getPersistentID() UUID}
     *
     * @return The back location or {@code null} if one doesn't exist
     */
    @Nullable
    public Location getBackLocation(final UUID uuid) {
        return backMap.remove(uuid);
    }

    public void setBackLocation(final UUID uuid, final Location location) {
        backMap.put(uuid, location);
    }

    public static BackHandler instance() {
        return ServerTools.instance.backHandler;
    }

    @SubscribeEvent
    public void onPlayerDeath(final LivingDeathEvent event) {
        if (event.entity instanceof EntityPlayerMP) {
            this.setBackLocation(event.entity.getPersistentID(), new Location(event.entity));
        }
    }
}
