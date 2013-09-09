/*
 * (C) Copyright 2013 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *      Delbosc Benoit
 */
package org.nuxeo.common.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper to log information that can be displayed using plantuml to render
 * sequence UML diagram.
 *
 *
 */
public class SequenceTracer {

    private static final Log log = LogFactory.getLog(SequenceTracer.class);

    private static final String PREFIX = "@@ ";

    // Utility class.
    private SequenceTracer() {
    }

    /**
     * Mark an event.
     */
    public static void mark(String message) {
        if (!log.isDebugEnabled()) {
            return;
        }
        final String tn = getThreadName();
        log.debug(PREFIX + tn + " -> " + tn + ": " + message);
    }

    /**
     * Mark the beginning of an action
     */
    public static void start(String message) {
        if (!log.isDebugEnabled()) {
            return;
        }
        final String tn = getThreadName();
        log.debug(PREFIX + tn + " -> " + tn + ": " + sanitize(message) + "\n"
                + PREFIX + "activate " + tn);
    }

    /**
     * Mark the beginning of an action initiated by the caller.
     */
    public static void startFrom(final String callerThread, final String message) {
        if (!log.isDebugEnabled()) {
            return;
        }
        final String tn = getThreadName();
        log.debug(PREFIX + callerThread + " --> " + tn + ": Initiate\n" + PREFIX
                + tn + " -> " + tn + ": " + sanitize(message) + "\n" + PREFIX
                + "activate " + tn);
    }

    private static String sanitize(String message) {
        return message.replace(", ", ",\\n").replace("-", "_");
    }

    /**
     * Mark the end of the previous action.
     */
    public static void stop(String message) {
        if (!log.isDebugEnabled()) {
            return;
        }
        final String tn = getThreadName();
        log.debug(PREFIX + tn + " -> " + tn + ": " + sanitize(message) + "\n"
                + PREFIX + "deactivate " + tn);
    }

    /**
     * Mark the last action as failure
     */
    public static void destroy(String message) {
        if (!log.isDebugEnabled()) {
            return;
        }
        final String tn = getThreadName();
        log.debug(PREFIX + tn + " -> " + tn + ": " + sanitize(message) + "\n"
                + PREFIX + "destroy " + tn);
    }

    /**
     * Add a note on the current thread
     */
    public static void addNote(String message) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug(PREFIX + "note right of " + getThreadName() + ": " + message);
    }

    /**
     * Link from source to current thread.
     */
    public static void addRelation(String source, String message) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug(PREFIX + source + " --> " + getThreadName() + ": "
                + sanitize(message));
    }

    /**
     * Get the thread name sanitized for plantuml
     */
    public static String getThreadName() {
        return sanitize(Thread.currentThread().getName());
    }

}
