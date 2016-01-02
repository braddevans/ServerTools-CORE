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
package info.servertools.core.feature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class Features {

    private static final Logger log = LogManager.getLogger();

    private static final Map<Class<?>, Object> serviceMap = new IdentityHashMap<>();

    public static boolean isRegistered(final Class<?> clazz) {
        return serviceMap.containsKey(Objects.requireNonNull(clazz, "clazz"));
    }

    public static <T> Optional<T> getService(final Class<T> clazz) {
        return Optional.ofNullable(clazz.cast(serviceMap.get(clazz)));
    }

    public static <T> void register(final Class<T> clazz, final T featureInstance) {
        if (serviceMap.containsKey(clazz)) {
            throw new RuntimeException("Feature " + clazz + " was already registered");
        }
        serviceMap.put(clazz, featureInstance);
    }
}