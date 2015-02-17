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

public class Reference {

    /**
     * Our unique Mod ID
     */
    public static final String MOD_ID = "ServerTools";

    /**
     * The user-friendly name of the mod
     */
    public static final String MOD_NAME = MOD_ID;

    /**
     * Our dependencies
     */
    public static final String DEPENDENCIES = "required-after:Forge@[11.14.1.1319,)";

    /**
     * The file encoding we save and read with
     */
    public static final String FILE_ENCODING = "UTF-8";

    /**
     * The {@link Charset} that represents the {@link #FILE_ENCODING}
     */
    public static final Charset CHARSET = Charset.forName(FILE_ENCODING);
}
