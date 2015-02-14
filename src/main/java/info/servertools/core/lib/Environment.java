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
package info.servertools.core.lib;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLInjectionData;

import java.io.File;

/**
 * Reference values that will changed based on the installed environment
 */
public final class Environment {

    /**
     * The location where Minecraft is installed
     */
    public static final File MINECRAFT_DIR = (File) FMLInjectionData.data()[6];

    /**
     * The location where ServerTools configs are located
     */
    public static final File SERVERTOOLS_DIR = new File(Loader.instance().getConfigDir(), "servertools");



    static {
        SERVERTOOLS_DIR.mkdirs();
    }

    private Environment() {}
}
