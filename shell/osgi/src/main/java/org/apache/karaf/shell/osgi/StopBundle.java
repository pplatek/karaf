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

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.console.MultiException;
import org.osgi.framework.Bundle;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

@Command(scope = "osgi", name = "stop", description = "Stop bundle(s).")
public class StopBundle extends BundlesCommand {
	
	@Option(name = "-t", aliases={"--transient"}, description="Keep the bundle as auto-start", required = false, multiValued = false)
	boolean transientStop;
	
	protected void doExecute(List<Bundle> bundles) throws Exception {
        if (bundles.isEmpty()) {
            System.err.println("No bundles specified.");
            return;
        }
        List<Exception> exceptions = new ArrayList<Exception>();
        for (Bundle bundle : bundles) {
            try {
            	if (transientStop) {
            		bundle.stop(Bundle.STOP_TRANSIENT);
            	} else {
            		bundle.stop();
            	}
            } catch (Exception e) {
                exceptions.add(new Exception("Unable to stop bundle " + bundle.getBundleId() +
                        (e.getMessage() != null ? ": " + e.getMessage() : ""), e));
            }
        }
        MultiException.throwIf("Error stopping bundles", exceptions);
    }

}
