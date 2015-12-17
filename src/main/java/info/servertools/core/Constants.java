/*
 * This file is a part of ServerTools <http://servertools.info>
 *
 * Copyright (c) 2015 ServerTools
 * Copyright (c) 2015 contributors
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

import java.nio.charset.Charset;

@SuppressWarnings("WeakerAccess")
public final class Constants {

    public static final String MOD_ID = "ServerTools-CORE";
    public static final String MOD_NAME = MOD_ID;

    public static final String MC_VERSION = "1.8.8";
    public static final String DEPENDENCIES = "required-after:Forge@[11.15.0,)";

    public static final String CHARSET_NAME = "UTF-8";
    public static final Charset CHARSET = Charset.forName(CHARSET_NAME);

    private Constants() {}
}
