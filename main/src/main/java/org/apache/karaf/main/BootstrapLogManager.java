/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.karaf.main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Convenience class for configuring java.util.logging to append to
 * the configured log4j log.  This could be used for bootstrap logging
 * prior to start of the framework.
 * 
 */
public class BootstrapLogManager {
    private static Handler handler;
    private static final String KARAF_BOOTSTRAP_LOG = "karaf.bootstrap.log";

    private static Properties configProps;

    public static synchronized Handler getDefaultHandler () throws Exception {
        if (handler != null) {
            return handler;
        }
        String filename;
        File log;
        Properties props = new Properties();
        filename = configProps.getProperty(KARAF_BOOTSTRAP_LOG);

        if (filename != null) {
            log = new File(filename);
        } else {
            // Make a best effort to log to the default file appender configured for log4j
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(System.getProperty("karaf.etc") + "/org.ops4j.pax.logging.cfg");
                props.load(fis);
            } catch (IOException e) {
                props.setProperty("log4j.appender.out.file", "${karaf.data}/log/karaf.log");
            } finally {
                if (fis != null) { 
                    try {
                        fis.close(); 
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }

            if (props.getProperty("log4j.appender.out.file") == null) {
                // manage if the log4j.appender.out.file property is not present in
                // the etc/org.ops4j.pax.logging.cfg file
                props.setProperty("log4j.appender.out.file", "${karaf.data}/log/karaf.log");
            }
            filename = Main.substVars(props.getProperty("log4j.appender.out.file"),"log4j.appender.out.file", null, null);
            log = new File(filename);
        }


        return new BootstrapLogManager.SimpleFileHandler(log);

    }

    public static void setProperties(Properties configProps) {
        BootstrapLogManager.configProps = configProps;
    }


    /**
     * Implementation of java.util.logging.Handler that does simple appending
     * to a named file.  Should be able to use this for bootstrap logging
     * via java.util.logging prior to startup of pax logging.
     */
    public static class SimpleFileHandler extends StreamHandler {

        public SimpleFileHandler (File file) throws IOException {
            open(file, true);
        }

        private void open (File logfile, boolean append) throws IOException {
            if (!logfile.getParentFile().exists()) {
                try {
                    logfile.getParentFile().mkdirs();
                } catch (SecurityException se) {
                    throw new IOException(se.getMessage());
                }
            }
            FileOutputStream fout = new FileOutputStream(logfile, append);
            BufferedOutputStream out = new BufferedOutputStream(fout);
            setOutputStream(out);
        }

        public synchronized void publish (LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }
            super.publish(record);
            flush();
        }
    }


}
