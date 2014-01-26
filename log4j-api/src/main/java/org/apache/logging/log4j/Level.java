/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j;

import org.apache.logging.log4j.spi.StandardLevel;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Levels used for identifying the severity of an event. Levels are organized from most specific to least:
 * <ul>
 * <li>{@link #OFF} (most specific)</li>
 * <li>{@link #FATAL}</li>
 * <li>{@link #ERROR}</li>
 * <li>{@link #WARN}</li>
 * <li>{@link #INFO}</li>
 * <li>{@link #DEBUG}</li>
 * <li>{@link #TRACE}</li>
 * <li>{@link #ALL} (least specific)</li>
 * </ul>
 *
 * Typically, configuring a level in a filter or on a logger will cause logging events of that level and those
 * that are more specific to pass through the filter.
 * A special level, {@link #ALL}, is guaranteed to capture all levels when used in logging configurations.
 */
public abstract class Level implements Comparable<Level>, Serializable {

    private static final long serialVersionUID = 3077535362528045615L;
    private static final ConcurrentMap<String, Level> levels = new ConcurrentHashMap<String, Level>();
    private static final Object constructorLock = new Object();
    private static int ordinalCount = 0;

    /**
     * No events will be logged.
     */
    public static Level OFF;
    /**
     * A severe error that will prevent the application from continuing.
     */
    public static Level FATAL;
    /**
     * An error in the application, possibly recoverable.
     */
    public static Level ERROR;
    /**
     * An event that might possible lead to an error.
     */
    public static Level WARN;
    /**
     * An event for informational purposes.
     */
    public static Level INFO;
    /**
     * A general debugging event.
     */
    public static Level DEBUG;
    /**
     * A fine-grained debug message, typically capturing the flow through the application.
     */
    public static Level TRACE;
    /**
     * All events should be logged.
     */
    public static Level ALL;

    static {
        OFF = new Level("OFF", StandardLevel.OFF.intLevel()){};
        FATAL = new Level("FATAL", StandardLevel.FATAL.intLevel()){};
        ERROR = new Level("ERROR", StandardLevel.ERROR.intLevel()){};
        WARN = new Level("WARN", StandardLevel.WARN.intLevel()){};
        INFO = new Level("INFO", StandardLevel.INFO.intLevel()){};
        DEBUG = new Level("DEBUG", StandardLevel.DEBUG.intLevel()){};
        TRACE = new Level("TRACE", StandardLevel.TRACE.intLevel()){};
        ALL = new Level("ALL", StandardLevel.ALL.intLevel()){};
    }

    private final String name;
    private final int intLevel;
    private final int ordinal;
    private final StandardLevel standardLevel;

    protected Level(String name, int intLevel) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Illegal null Level constant");
        }
        if (intLevel < 0) {
            throw new IllegalArgumentException("Illegal Level int less than zero.");
        }
        this.name = name;
        this.intLevel = intLevel;
        this.standardLevel = StandardLevel.getStandardLevel(intLevel);
        synchronized(constructorLock) {
            if (levels.containsKey(name)) {
                throw new IllegalArgumentException("Level " + name + " has already been defined.");
            } else {
                ordinal = ordinalCount++;
                levels.put(name, this);
            }
        }
    }

    public final int intLevel() {
        return this.intLevel;
    }

    public final int ordinal() {
        return this.ordinal;
    }

    public final StandardLevel getStandardLevel() {
        return standardLevel;
    }

    /**
     * Compares this level against the level passed as an argument and returns true if this
     * level is the same or more specific.
     *
     * @param level The level to check.
     * @return True if the passed Level is more specific or the same as this Level.
     */
    public final boolean isAtLeastAsSpecificAs(final Level level) {
        return this.intLevel <= level.intLevel;
    }

    /**
     * Compares this level against the level passed as an argument and returns true if this
     * level is the same or more specific.
     *
     * @param level The level to check.
     * @return True if the passed Level is more specific or the same as this Level.
     */
    public final boolean isAtLeastAsSpecificAs(final int level) {
        return this.intLevel <= level;
    }

    /**
     * Compares the specified Level against this one.
     * @param level The level to check.
     * @return True if the passed Level is more specific or the same as this Level.
     */
    public final boolean lessOrEqual(final Level level) {
        return this.intLevel <= level.intLevel;
    }

    /**
     * Compares the specified Level against this one.
     * @param level The level to check.
     * @return True if the passed Level is more specific or the same as this Level.
     */
    public final boolean lessOrEqual(final int level) {
        return this.intLevel <= level;
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public final Level clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public final int compareTo(Level other) {
        return intLevel < other.intLevel ? -1 : (intLevel > other.intLevel ? 1 : 0);
    }

    @Override
    public final boolean equals(Object other) {
        return other instanceof Level && other == this;
    }

    public final Class<Level> getDeclaringClass() {
        return Level.class;
    }

    @Override
    public final int hashCode() {
        return this.name.hashCode();
    }


    public final String name() {
        return this.name;
    }

    @Override
    public final String toString() {
        return this.name;
    }

    /**
     * Return the Level assoicated with the name or null if the Level cannot be found.
     * @param name The name of the Level.
     * @return The Level or null.
     */
    public static Level getLevel(String name) {
        return levels.get(name);
    }

    /**
     * Converts the string passed as argument to a level. If the
     * conversion fails, then this method returns {@link #DEBUG}.
     *
     * @param sArg The name of the desired Level.
     * @return The Level associated with the String.
     */
    public static Level toLevel(final String sArg) {
        return toLevel(sArg, Level.DEBUG);
    }

    /**
     * Converts the string passed as argument to a level. If the
     * conversion fails, then this method returns the value of
     * <code>defaultLevel</code>.
     *
     * @param name The name of the desired Level.
     * @param defaultLevel The Level to use if the String is invalid.
     * @return The Level associated with the String.
     */
    public static Level toLevel(final String name, final Level defaultLevel) {
        if (name == null) {
            return defaultLevel;
        }
        Level level = levels.get(name.toUpperCase(Locale.ENGLISH));
        return level == null ? defaultLevel : level;
    }

    public static Level[] values() {
        return Level.levels.values().toArray(new Level[Level.levels.size()]);
    }


    public static Level valueOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Unknown level constant [" + name + "].");
        }
        name = name.toUpperCase();
        if (levels.containsKey(name)) {
            return levels.get(name);
        }
        throw new IllegalArgumentException("Unknown level constant [" + name + "].");
    }

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        return Enum.valueOf(enumType, name);
    }

    // for deserialization
    protected final Object readResolve() throws ObjectStreamException {
        return Level.valueOf(this.name);
    }
}

