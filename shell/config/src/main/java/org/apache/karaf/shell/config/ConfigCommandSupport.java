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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.utils.properties.Properties;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Abstract class from which all commands related to the ConfigurationAdmin
 * service should derive.
 * This command retrieves a reference to the ConfigurationAdmin service before
 * calling another method to actually process the command.
 */
public abstract class ConfigCommandSupport extends OsgiCommandSupport {

    public static final String PROPERTY_CONFIG_PID = "ConfigCommand.PID";
    public static final String PROPERTY_CONFIG_PROPS = "ConfigCommand.Props";
    private static final String PID_FILTER = "(service.pid=%s*)";
    private static final String CONFIG_SUFFIX = ".cfg";
    private static final String FACTORY_SEPARATOR = "-";
    private static final String FILEINSTALL_FILE_NAME = "felix.fileinstall.filename";

    protected File storage;
    private List<ArtifactInstaller> artifactInstallers;

    protected Object doExecute() throws Exception {
        // Get config admin service.
        ServiceReference ref = getBundleContext().getServiceReference(ConfigurationAdmin.class.getName());
        if (ref == null) {
            System.out.println("ConfigurationAdmin service is unavailable.");
            return null;
        }
        ConfigurationAdmin admin = getConfigurationAdmin();
        if (admin == null) {
            System.out.println("ConfigAdmin service is unavailable.");
            return null;
        }

        doExecute(admin);
        return null;
    }

    protected Dictionary<String, Object> getEditedProps() throws Exception {
        return (Dictionary<String, Object>) this.session.get(PROPERTY_CONFIG_PROPS);
    }

    protected ConfigurationAdmin getConfigurationAdmin() {
        ServiceReference ref = getBundleContext().getServiceReference(ConfigurationAdmin.class.getName());
        if (ref == null) {
            return null;
        }
        try {
            ConfigurationAdmin admin = (ConfigurationAdmin) getBundleContext().getService(ref);
            if (admin == null) {
                return null;
            } else {
                return admin;
            }
        } finally {
            getBundleContext().ungetService(ref);
        }
    }

    protected abstract void doExecute(ConfigurationAdmin admin) throws Exception;

    /**
     * <p>
     * Returns the Configuration object of the given (felix fileinstall) file name.
     * </p>
     *
     * @param fileName
     * @return
     */
    public Configuration findConfigurationByFileName(ConfigurationAdmin admin, String fileName) throws IOException, InvalidSyntaxException {
        if (fileName != null) {
            String factoryPid = fileName;
            if (fileName.contains(FACTORY_SEPARATOR)) {
                factoryPid = fileName.substring(0, fileName.lastIndexOf(FACTORY_SEPARATOR));
            }
            Configuration[] configurations = admin.listConfigurations(String.format(PID_FILTER, factoryPid));
            if (configurations != null) {
                for (Configuration configuration : configurations) {
                    Dictionary dictionary = configuration.getProperties();
                    if (dictionary != null) {
                        String fileInstallFileName = (String) dictionary.get(FILEINSTALL_FILE_NAME);
                        int indexOfFileNameStart = fileInstallFileName.lastIndexOf("/");
                        int indexOfFileNameEnd = fileInstallFileName.lastIndexOf(CONFIG_SUFFIX);
                        String relativeFileName = fileInstallFileName.substring(indexOfFileNameStart + 1, indexOfFileNameEnd);
                        if (fileName.equals(relativeFileName)) {
                            return configuration;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Saves config to storage or ConfigurationAdmin.
     *
     * @param admin
     * @param pid
     * @param props
     * @param bypassStorage
     * @throws IOException
     */
    protected void update(ConfigurationAdmin admin, String pid, Dictionary<String, Object> props, boolean bypassStorage) throws IOException {
        if (!bypassStorage && storage != null) {
            persistConfiguration(admin, pid, props);
        } else {
            updateConfiguration(admin, pid, props);
        }
    }

    /**
     * Persists configuration to storage.
     *
     * @param admin
     * @param pid
     * @param props
     * @throws IOException
     */
    protected void persistConfiguration(ConfigurationAdmin admin, String pid, Dictionary<String, Object> props) throws IOException {
        File storageFile = new File(storage, pid + ".cfg");
        Configuration cfg = admin.getConfiguration(pid, null);
        if (cfg != null && cfg.getProperties() != null) {
            Object val = cfg.getProperties().get(FILEINSTALL_FILE_NAME);
            try {
            if (val instanceof URL) {
                storageFile = new File(((URL) val).toURI());
            }
            if (val instanceof URI) {
                storageFile = new File((URI) val);
            }
            if (val instanceof String) {
                storageFile = new File(new URL((String) val).toURI());
            }
            } catch (Exception e) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }
        }
        Properties p = new Properties(storageFile);
        for (Enumeration<String> keys = props.keys(); keys.hasMoreElements(); ) {
            String key = keys.nextElement();
            if (!Constants.SERVICE_PID.equals(key)
                    && !ConfigurationAdmin.SERVICE_FACTORYPID.equals(key)
                    && !FILEINSTALL_FILE_NAME.equals(key)) {
                if (props.get(key) != null) {
                    p.put(key, props.get(key).toString());
                }
            }
        }
        // remove "removed" properties from the file
        ArrayList<String> propertiesToRemove = new ArrayList<String>();
        for (String key : p.keySet()) {
            if (props.get(key) == null
                    && !Constants.SERVICE_PID.equals(key)
                    && !ConfigurationAdmin.SERVICE_FACTORYPID.equals(key)
                    && !FILEINSTALL_FILE_NAME.equals(key)) {
                propertiesToRemove.add(key);
            }
        }
        for (String key : propertiesToRemove) {
            p.remove(key);
        }
        // save the cfg file
        storage.mkdirs();
        p.save();
        updateFileInstall(storageFile);
    }

    /**
     * Trigger felix fileinstall to update the config so there is no delay till it polls the file
     * 
     * @param storageFile
     * @throws Exception
     */
    private void updateFileInstall(File storageFile) {
        if (artifactInstallers != null) {
            for (ArtifactInstaller installer : artifactInstallers) {
                if (installer.canHandle(storageFile)) {
                    try {
                        installer.update(storageFile);
                    } catch (Exception e) {
                        log.warn("Error updating config " + storageFile + " in felix fileinstall" + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Updates the configuration to the {@link ConfigurationAdmin} service.
     *
     * @param admin
     * @param pid
     * @param props
     * @throws IOException
     */
    public void updateConfiguration(ConfigurationAdmin admin, String pid, Dictionary<String, Object> props) throws IOException {
        Configuration cfg = admin.getConfiguration(pid, null);
        if (cfg.getProperties() == null) {
            String[] pids = parsePid(pid);
            if (pids[1] != null) {
                cfg = admin.createFactoryConfiguration(pids[0], null);
            }
        }
        cfg.update(props);
    }

    protected String[] parsePid(String pid) {
        int n = pid.indexOf('-');
        if (n > 0) {
            String factoryPid = pid.substring(n + 1);
            pid = pid.substring(0, n);
            return new String[]{pid, factoryPid};
        } else {
            return new String[]{pid, null};
        }
    }

    protected void deleteStorage(String pid) throws Exception {
        File cfgFile = new File(storage, pid + ".cfg");
        cfgFile.delete();
    }

    public File getStorage() {
        return storage;
    }

    public void setStorage(File storage) {
        this.storage = storage;
    }
    
    public void setArtifactInstallers(List<ArtifactInstaller> artifactInstallers) {
        this.artifactInstallers = artifactInstallers;
    }
}
