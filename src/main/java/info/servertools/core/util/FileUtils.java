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

import static java.nio.file.FileVisitResult.CONTINUE;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class FileUtils {

    /**
     * Get the size of a file or directory
     *
     * @param file the file or directory to measure
     *
     * @return the size in bytes
     */
    public static long getSize(final Path file) throws IOException {
        if (Files.isRegularFile(file)) {
            return Files.size(file);
        } else {
            final TLongList sizes = new TLongArrayList();
            Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException {
                    sizes.add(attrs.size());
                    return CONTINUE;
                }
            });

            long sum = 0;
            for (final long val : sizes.toArray()) {
                sum += val;
            }
            return sum;
        }
    }

    private FileUtils() {}
}
