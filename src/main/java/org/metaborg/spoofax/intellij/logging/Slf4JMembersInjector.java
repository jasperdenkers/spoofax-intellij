/*
 * Copyright © 2015-2015
 *
 * This file is part of Spoofax for IntelliJ.
 *
 * Spoofax for IntelliJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoofax for IntelliJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Spoofax for IntelliJ.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.metaborg.spoofax.intellij.logging;

import com.google.inject.MembersInjector;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Injects loggers.
 *
 * @param <T> The type of object.
 */
public final class Slf4JMembersInjector<T> implements MembersInjector<T> {

    @NotNull
    private final Field field;
    @NotNull
    private final Logger logger;

    /**
     * Initializes a new instance of the {@link Slf4JMembersInjector} class.
     *
     * @param field The field to inject.
     */
    public Slf4JMembersInjector(@NotNull Field field) {
        this.field = field;
        this.logger = createLogger(field.getDeclaringClass());
        this.field.setAccessible(true);
    }

    /**
     * Creates the logger.
     *
     * @param clazz The class.
     * @return The created logger instance.
     */
    @NotNull
    private static Logger createLogger(@NotNull Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Injects the logger in the specified object.
     *
     * @param obj The object.
     */
    @Override
    public void injectMembers(@NotNull T obj) {
        try {
            this.field.set(obj, logger);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
