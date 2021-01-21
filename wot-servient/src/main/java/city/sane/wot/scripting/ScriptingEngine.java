/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.scripting;

import city.sane.wot.Wot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * A ScriptingEngine describes how a WoT script can be executed in a certain scripting language.
 */
interface ScriptingEngine {
    /**
     * Returns the media type supported by the codec (e.g. application/javascript).
     *
     * @return
     */
    String getMediaType();

    /**
     * Returns the file extension supported by the codec (e.g. .js).
     *
     * @return
     */
    String getFileExtension();

    /**
     * Runs <code>script</code> in sandboxed context.
     *
     * @param script
     * @param wot
     * @param executorService
     * @return
     */
    CompletableFuture<Void> runScript(String script, Wot wot, ExecutorService executorService);

    /**
     * Runs <code>script</code> in privileged context (dangerous) - means here: Script can import
     * classes and make system calls.
     *
     * @param script
     * @param wot
     * @param executorService
     * @return
     */
    CompletableFuture<Void> runPrivilegedScript(String script,
                                                Wot wot,
                                                ExecutorService executorService);
}
