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

import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.features.FeaturesService;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.MultiException;

@Command(scope = "features", name = "addUrl", description = "Adds a list of repository URLs to the features service")
public class AddUrlCommand extends FeaturesCommandSupport {

    @Argument(index = 0, name = "urls", description = "One or more repository URLs separated by whitespaces", required = true, multiValued = true)
    List<String> urls;

    @Option(name = "-i", aliases = { "--install-all" }, description = "Install all features contained in the repository URLs", required = false, multiValued = false)
    boolean install;

    protected void doExecute(FeaturesService admin) throws Exception {
        List<Exception> exceptions = new ArrayList<Exception>();
        for (String url : urls) {
            try {
                admin.addRepository(new URI(url), install);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        MultiException.throwIf("Unable to add repositories", exceptions);
    }
}
