package info.servertools.core.teleport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.StandardOpenOption.CREATE;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import info.servertools.core.ServerTools;
import info.servertools.core.lib.Reference;
import info.servertools.core.util.Location;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

public class HomeHandler {

    private static final Logger log = LogManager.getLogger();

    private final Path saveFile;
    private final Lock fileLock = new ReentrantLock();

    private final Map<UUID, Map<Integer, Location>> homeMap = new HashMap<>();
    private static final Type type = new TypeToken<HashMap<UUID, HashMap<Integer, Location>>>() {}.getType();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public HomeHandler(final Path saveFile) {
        this.saveFile = checkNotNull(saveFile, "saveFile").normalize();
        checkArgument(!Files.exists(this.saveFile) || Files.isRegularFile(this.saveFile), "Directory exists with same name as home save file: " + this.saveFile);
        this.load();
    }

    /**
     * Get a player's home location
     *
     * @param uuid  The player's {@link EntityPlayerMP#getPersistentID() UUID}
     * @param dimID The dimension ID
     *
     * @return The player's home, or {@code null} if no home was set for the given dimension
     */
    @Nullable
    public Location getHome(final UUID uuid, final int dimID) {
        @Nullable
        Map<Integer, Location> homes = homeMap.get(uuid);
        if (homes == null) {
            return null;
        }
        return homes.get(dimID);
    }

    /**
     * Set a player's home location
     *
     * @param uuid     The player's {@link EntityPlayerMP#getPersistentID() UUID}
     * @param location The location
     *
     * @return {@code true} if an existing home was replaced, {@code false} if no previous home existed
     */
    public boolean setHome(final UUID uuid, final int dimID, @Nullable final Location location) {
        @Nullable
        Map<Integer, Location> homes = homeMap.get(uuid);
        if (homes == null) {
            homes = new HashMap<>();
            homeMap.put(uuid, homes);
        }

        @Nullable Location old = homes.put(dimID, location);
        save();
        return old != null;
    }


    /**
     * Get an {@link ImmutableMap immutable} copy of all the homes
     *
     * @return All the homes
     */
    public ImmutableMap<UUID, Map<Integer, Location>> getHomeMap() {
        return ImmutableMap.copyOf(this.homeMap);
    }

    private void save() {
        final String json = gson.toJson(homeMap, type);
        ServerTools.executorService.submit(new Runnable() {
            @Override
            public void run() {
                fileLock.lock();
                try (final BufferedWriter writer = Files.newBufferedWriter(saveFile, Reference.CHARSET, CREATE)) {
                    writer.write(json);
                } catch (IOException e) {
                    log.error("Failed to save homes to disk", e);
                } finally {
                    fileLock.unlock();
                }
            }
        });
    }

    private void load() {
        if (!Files.exists(this.saveFile)) {
            return;
        }
        fileLock.lock();
        try (final BufferedReader reader = Files.newBufferedReader(saveFile, Reference.CHARSET)) {
            this.homeMap.clear();
            final Map<UUID, Map<Integer, Location>> map = gson.fromJson(reader, type);
            this.homeMap.putAll(map);
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse home file {} as valid JSON", saveFile, e);
        } catch (IOException e) {
            log.error("Failed to load home file {} from disk", saveFile, e);
        } finally {
            fileLock.unlock();
        }
    }
}
