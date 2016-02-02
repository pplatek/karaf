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

import jline.console.ConsoleReader;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;

@Command(scope = "osgi", name = "bundle-level", description = "Gets or sets the start level of a given bundle.")
public class BundleLevel extends BundleCommand {

    @Argument(index = 1, name = "startLevel", description = "The bundle's new start level", required = false, multiValued = false)
    Integer level;

    protected void doExecute(Bundle bundle) throws Exception {
        // Get package admin service.
        ServiceReference ref = getBundleContext().getServiceReference(StartLevel.class.getName());
        if (ref == null) {
            System.out.println("StartLevel service is unavailable.");
            return;
        }
        StartLevel sl = getService(StartLevel.class, ref);
        if (sl == null) {
            System.out.println("StartLevel service is unavailable.");
            return;
        }

        if (level == null) {
            System.out.println("Level " + sl.getBundleStartLevel(bundle));
        }
        else if ((level < 50) && (sl.getBundleStartLevel(bundle) > 50) && !force){
            for (;;) {
                ConsoleReader reader = (ConsoleReader) session.get(".jline.reader");
                String msg = "You are about to designate bundle as a system bundle.  Do you wish to continue (yes/no): ";
                String str = reader.readLine(msg);
                if ("yes".equalsIgnoreCase(str)) {
                    sl.setBundleStartLevel(bundle, level);
                    break;
                } else if ("no".equalsIgnoreCase(str)) {
                    break;
                }
            }
        } else {
            sl.setBundleStartLevel(bundle, level);
        }
    }

}
