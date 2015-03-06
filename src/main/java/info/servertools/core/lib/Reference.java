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

import java.nio.charset.Charset;

/**
 * Data that will be the same, no matter what system/instance ServerTools is installed in
 */
public final class Reference {

    /**
     * Our unique Mod ID
     */
    public static final String MOD_ID = "ServerTools";

    /**
     * The user-friendly name of the mod
     */
    public static final String MOD_NAME = MOD_ID;

    /**
     * The version of MinecraftForge that the current version of ServerTools requires
     */
    public static final String FORGE_REQ = "11.14.1.1319";

    /**
     * The mod dependencies that ServerTools has
     */
    public static final String DEPENDENCIES = "required-after:Forge@[" + FORGE_REQ + ",)";

    /**
     * The file encoding that ServerTools uses for IO
     */
    public static final String FILE_ENCODING = "UTF-8";

    /**
     * The {@link Charset} that represents the {@link #FILE_ENCODING}
     */
    public static final Charset CHARSET = Charset.forName(FILE_ENCODING);

    /**
     * The line separator sequence that ServerTools uses
     */
    public static final String LINE_SEPARATOR = "\n";

    private Reference() {}
}
