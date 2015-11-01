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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class FileIO {

    private static final ExecutorService service = Executors.newSingleThreadExecutor(r -> new Thread(r, "ServerTools IO Thread"));

    public static Future<?> submitTask(Runnable runnable) {
        return service.submit(runnable);
    }

    public static <T> Future<T> submitTask(Callable<T> callable) {
        return service.submit(callable);
    }

    public static void shutDown() {
        service.shutdown();
        try {
            service.awaitTermination(10, SECONDS);
        } catch (InterruptedException ignored) {}
    }

    private FileIO() {}
}
