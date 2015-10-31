package info.servertools.core;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(
        modid = ServerToolsCore.MOD_ID,
        name = ServerToolsCore.MOD_NAME,
        acceptableRemoteVersions = "*", // Don't require mod on client
        acceptedMinecraftVersions = '[' + ServerToolsCore.MC_VERSION + ']'
)
public final class ServerToolsCore {

    public static final String MOD_ID = "ServerTools-CORE";
    public static final String MOD_NAME = MOD_ID;
    public static final String MC_VERSION = "1.8";

    private static Path configDir;

    private static STConfig<CoreConfig> coreConfig;

    @Mod.EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) throws IOException {
        final File modConfigDir = event.getModConfigurationDirectory();
        configDir = modConfigDir.toPath().resolve("ServerTools-CORE");
        Files.createDirectories(configDir);
        coreConfig = new STConfig<>(configDir.resolve("ServerTools-CORE.conf"), CoreConfig.class);
    }

    @Mod.EventHandler
    public void onServerStarted(final FMLServerStartedEvent event) {

    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static CoreConfig getConfig() {
        return coreConfig.getConfig();
    }
}
