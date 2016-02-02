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
package org.apache.karaf.shell.osgi;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.gogo.commands.Argument;
import org.osgi.framework.Bundle;

public abstract class BundleCommand extends OsgiCommandSupport {

    @Argument(index = 0, name = "id", description = "The bundle ID or name or name/version", required = true, multiValued  = false)
    String id;

    @Option(name = "--force", aliases = {}, description = "Forces the command to execute", required = false, multiValued = false)
    boolean force;

    protected Object doExecute() throws Exception {
        BundleSelector selector = new BundleSelector(getBundleContext(), session);
        Bundle bundle = selector.getBundle(id, force);

        if (bundle == null) {
            System.err.println("Bundle " + id + " not found");
            return null;
        }

        doExecute(bundle);
        return null;
    }

    protected abstract void doExecute(Bundle bundle) throws Exception;
}
