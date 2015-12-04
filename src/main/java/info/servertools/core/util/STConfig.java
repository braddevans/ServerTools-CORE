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
package info.servertools.core.util;

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

/**
 * Base class for ServerTools config. Built on Configurate by zml
 *
 * @param <T> The type that holds the configuration options
 */
public class STConfig<T> {

    private static final Logger log = LogManager.getLogger();

    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode root = SimpleCommentedConfigurationNode.root();
    private ObjectMapper<T>.BoundInstance configMapper;
    private T configBase;
    private final Path file;

    public STConfig(final Path file, final Class<T> clazz) throws IOException, ObjectMappingException {
        this.file = file;
        try {
            if (!Files.exists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }

            if (!Files.exists(file)) {
                Files.createFile(file);
            }

            this.loader = HoconConfigurationLoader.builder().setPath(file).build();
            this.configMapper = ObjectMapper.forClass(clazz).bindToNew();

            load();
            save();
        } catch (Exception e) {
            log.error("Failed to create config file {}", file, e);
            throw e;
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
