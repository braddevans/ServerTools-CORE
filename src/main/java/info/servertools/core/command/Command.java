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
package info.servertools.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker interface to auto-register {@linkplain STCommand ServerTools Commands}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * Get the default name that this command should be registered as. This name can be changed via a user-facing configuration file.
     *
     * @return The default name
     */
    String name();

    /**
     * Get if server operator status is required to execute the command. This can be changed via a user-facing configuration file.
     *
     * @return If op is required
     */
    boolean opRequired();

    /**
     * An array of feature classes that must be enabled for this command to be enabled. If all of the features are not available, the command will
     * never be instantiated.
     *
     * @return An array of required feature classes
     */
    Class<?>[] requiredFeatures() default {};
}
