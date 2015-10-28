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

package org.metaborg.core.project.settings;

import org.jetbrains.annotations.NotNull;
import org.metaborg.core.StringFormatter;

/**
 * A setting key.
 *
 * Be sure to store and reuse the <em>exact same key object</em>,
 * as different instances are not equal (even if they have the same name and type).
 */
public final class SettingKey<T> {

    @NotNull
    private final String name;
    @NotNull
    private final Class<T> type;

    /**
     * Initializes a new instance of the {@link SettingKey} class.
     *
     * @param name The name of the setting.
     * @param type The type of the setting.
     */
    public SettingKey(@NotNull final String name, @NotNull final Class<T> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the name of the setting.
     *
     * @return The name of the setting.
     */
    @NotNull
    public String name() {
        return this.name;
    }

    /**
     * Gets the type of the setting.
     *
     * @return The type of the setting.
     */
    @NotNull
    public Class<T> type() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        // NOTE: Reference equality.
        return this == obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        // NOTE: The hash code never changes for this object.
        return System.identityHashCode(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return StringFormatter.format("{}<{}>", this.name, this.type.getName());
    }
}
