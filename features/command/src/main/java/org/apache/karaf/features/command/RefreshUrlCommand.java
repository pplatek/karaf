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
package org.apache.karaf.features.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.Repository;
import org.apache.karaf.shell.console.MultiException;

@Command(scope = "features", name = "refreshUrl", description = "Reloads the list of available features from the repositories.")
public class RefreshUrlCommand extends FeaturesCommandSupport {

    @Argument(index = 0, name = "urls", description = "Repository URLs to reload (leave empty for all)", required = false, multiValued = true)
    List<String> urls;

    protected void doExecute(FeaturesService admin) throws Exception {
        if (urls == null || urls.isEmpty()) {
            urls = new ArrayList<String>();
            for (Repository repo : admin.listRepositories()) {
                urls.add(repo.getURI().toString());
            }
        }
        List<Exception> exceptions = new ArrayList<Exception>();
        for (String strUri : urls) {
            try {
                URI uri = new URI(strUri);
                admin.removeRepository(uri);
                admin.addRepository(uri);
            } catch (Exception e) {
                exceptions.add(e);
                //get chance to restore previous, fix for KARAF-4
                admin.restoreRepository(new URI(strUri));
            }
        }
        MultiException.throwIf("Unable to add repositories", exceptions);
    }
}
