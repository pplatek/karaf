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
package org.apache.karaf.shell.config;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@Command(scope = "config", name = "delete", description = "Delete a configuration.")
public class DeleteCommand extends ConfigCommandSupport {

    @Argument(index = 0, name = "pid", description = "PID of the configuration", required = true, multiValued = false)
    String pid;

    @Option(name = "--force", aliases = {}, description = "Force the edition of this config, even if another one was under edition", required = false, multiValued = false)
    boolean force;

    @Option(name = "--no-delete-cfg-file", aliases = { }, description = "Do not delete the configuration file from the etc folder", required = false, multiValued = false)
    boolean noDeleteCfgFile = false;

    @Option(name = "-f", aliases = {"--use-file"}, description = "Configuration lookup using the filename instead of the pid", required = false, multiValued = false)
    boolean useFile;

    protected void doExecute(ConfigurationAdmin admin) throws Exception {
        String oldPid = (String) this.session.get(PROPERTY_CONFIG_PID);
        if (oldPid != null && oldPid.equals(pid) && !force) {
            System.err.println("This config is being edited.  Cancel / update first, or use the --force option");
            return;
        }

        // User selected to use file instead.
        if (useFile) {
            Configuration configuration = this.findConfigurationByFileName(admin, pid);
            if(configuration == null) {
                System.err.println("Could not find configuration with file install property set to: " + pid);
                return;
            }
            configuration.delete();
        } else {
            Configuration configuration = admin.getConfiguration(pid);
            configuration.delete();
        }
        if (!noDeleteCfgFile) {
            deleteStorage(pid);
        }
        if (oldPid != null && oldPid.equals(pid) && !force) {
            this.session.put(PROPERTY_CONFIG_PID, null);
            this.session.put(PROPERTY_CONFIG_PROPS, null);
        }
    }

}
