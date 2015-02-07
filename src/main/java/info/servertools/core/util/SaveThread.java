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

import info.servertools.core.lib.Reference;

import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

public class SaveThread extends Thread {

    protected final Logger log = LogManager.getLogger();

    public final String data;
    @Nullable
    private File file;

    public SaveThread(String data) {
        this.data = data;
    }

    public SaveThread(String data, @Nullable File file) {
        this.data = data;
        this.file = file;
    }

    @Override
    public void run() {
        if (file != null) {
            try {
                Files.write(data, file, Reference.CHARSET);
            } catch (IOException e) {
                log.warn("Failed to save file to disk", e);
            }
        }
    }
}
