/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.karaf.features.internal;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.karaf.features.FeaturesNamespaces;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility class which fires XML Schema validation.
 */
public class FeatureValidationUtil {

    /**
     * Runs schema validation.
     * 
     * @param uri Uri to validate.
     * @throws Exception When validation fails.
     */
    public static void validate(URI uri) throws Exception {
        URLConnection conn = null;
        try {
            conn = uri.toURL().openConnection();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid URI: " + uri, e);
        }
        conn.setDefaultUseCaches(false);

        InputStream stream = conn.getInputStream();

        // load document and check the root element for namespace declaration
        DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
        dFactory.setNamespaceAware(true);
        Document doc = dFactory.newDocumentBuilder().parse(stream);

        QName name = new QName(doc.getDocumentElement().getNamespaceURI(), doc.getDocumentElement().getLocalName());
        if (FeaturesNamespaces.FEATURES_0_0_0.equals(name)) {
            return;
        } else if (FeaturesNamespaces.FEATURES_1_0_0.equals(name)) {
            validate(doc, "/org/apache/karaf/features/karaf-features-1.0.0.xsd");
        } else if (FeaturesNamespaces.FEATURES_1_1_0.equals(name)) {
            validate(doc, "/org/apache/karaf/features/karaf-features-1.1.0.xsd");
        } else if (FeaturesNamespaces.FEATURES_1_2_0.equals(name)) {
            validate(doc, "/org/apache/karaf/features/karaf-features-1.2.0.xsd");
        } else if (FeaturesNamespaces.FEATURES_1_2_1.equals(name)) {
            validate(doc, "/org/apache/karaf/features/karaf-features-1.2.1.xsd");
        }
        else {
            throw new IllegalArgumentException("Unrecognized root element: " + name);
        }
    }


    private static void validate(Document doc, String schemaLocation) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        /** FIXME should move to 1.1.0 ? */
        // root element has namespace - we can use schema validation
        Schema schema = factory.newSchema(new StreamSource(FeatureValidationUtil.class
            .getResourceAsStream(schemaLocation)));

        // create schema by reading it from an XSD file:
        Validator validator = schema.newValidator();

        try {
            validator.validate(new DOMSource(doc));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to validate " + doc.getDocumentURI(), e);
        }        
    }

}
