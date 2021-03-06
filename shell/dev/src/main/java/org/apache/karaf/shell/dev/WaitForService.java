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

import java.util.concurrent.TimeoutException;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Command that can be used to wait for an OSGi service.
 */
@Command(scope = "dev", name = "wait-for-service", description = "Wait for a given OSGi service.")
public class WaitForService extends OsgiCommandSupport {

    @Option(name = "-e", aliases = { "--exception" }, description = "throw an exception if the service is not found after the timeout")
    boolean exception;

    @Option(name = "-t", aliases = { "--timeout" }, description = "timeout to wait for the service (in milliseconds, negative to not wait at all, zero to wait forever)")
    long timeout = 0;

    @Argument(name = "service", description="The service class or filter", required = true, multiValued = false)
    String service;

    @Override
    protected Object doExecute() throws Exception {
        ServiceTracker tracker = null;
        try {
            String filter = service;
            if (!filter.startsWith("(")) {
                if (!filter.contains("=")) {
                    filter = Constants.OBJECTCLASS + "=" + filter;
                }
                filter = "(" + filter + ")";
            }
            Filter osgiFilter = FrameworkUtil.createFilter(filter);
            tracker = new ServiceTracker(bundleContext, osgiFilter, null);
            tracker.open(true);
            Object svc = tracker.getService();
            if (timeout >= 0) {
                svc = tracker.waitForService(timeout);
            }
            if (exception && svc == null) {
                throw new TimeoutException("Can not find service '" + service + "' in the OSGi registry");
            }
            return svc != null;
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (tracker != null) {
                tracker.close();
            }
        }
    }

}
