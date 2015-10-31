package info.servertools.core;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class STConfig<T> {

    private static final Logger log = LogManager.getLogger();

    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode root = SimpleCommentedConfigurationNode.root();
    private ObjectMapper<T>.BoundInstance configMapper;
    private T configBase;
    private Path file;

    public STConfig(final Path file, final Class<T> clazz) {
        this.file = file;
        try {
            if (!Files.exists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }

            if (!Files.exists(file)) {
                Files.createFile(file);
            }

            this.loader = HoconConfigurationLoader.builder().setFile(file.toFile()).build();
            this.configMapper = ObjectMapper.forClass(clazz).bindToNew();

            load();
            save();
        } catch (Exception e) {
            log.error("Failed to create config file {}", file, e);
        }
    }

    public void save() {
        try {
            this.configMapper.serialize(this.root);
            this.loader.save(this.root);
        } catch (IOException | ObjectMappingException e) {
            log.error("Failed to save config file {}", this.file, e);
        }
    }

    public void load() {
        try {
            this.root = this.loader.load();
            this.configBase = this.configMapper.populate(this.root);
        } catch (IOException | ObjectMappingException e) {
            log.error("Failed to load config file {}", this.file, e);
        }
    }

    public T getConfig() {
        return this.configBase;
    }
}
