/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.shell.dev;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * A command to restart karaf
 */
@Command(scope = "dev", name = "restart", description = "Restart Karaf.")
public class Restart  extends OsgiCommandSupport {

    @Option(name = "-c", aliases = {"--clean", "--clean-all", "-ca"}, description = "Force a clean restart by deleting the working directory")
    private boolean cleanAll;

    @Option(name = "-cc", aliases = {"--clean-cache", "-cc"}, description = "Force a clean restart by deleting the working directory")
    private boolean cleanCache;


    @Override
    protected Object doExecute() throws Exception {
        System.setProperty("karaf.restart", "true");
        System.setProperty("karaf.clean.cache", Boolean.toString(cleanCache));
        System.setProperty("karaf.clean.all", Boolean.toString(cleanAll));
        bundleContext.getBundle(0).stop();
        return null;
    }
}
