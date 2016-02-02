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
package org.apache.karaf.admin.command;

import org.apache.karaf.admin.AdminService;
import org.apache.karaf.admin.Instance;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AdminCommandSupport extends OsgiCommandSupport {

    private AdminService adminService;

    public AdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    protected Instance getExistingInstance(String name) {
        Instance i = adminService.getInstance(name);
        if (i == null) {
            throw new IllegalArgumentException("Instance '" + name + "' does not exist");
        }
        return i;
    }

    protected static Map<String, URL> getResources(List<String> resources) throws MalformedURLException {
        Map<String, URL> result = new HashMap<String, URL>();
        if (resources != null) {
            for(String resource : resources) {
                String path = resource.substring(0, resource.indexOf("="));
                String location = resource.substring(path.length() + 1);
                URL url = new URL(location);
                result.put(path, url);
            }
        }
        return result;
    }
}
