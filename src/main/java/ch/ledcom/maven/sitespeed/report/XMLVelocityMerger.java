/**
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package ch.ledcom.maven.sitespeed.report;

import static ch.ledcom.maven.sitespeed.Configuration.MERGER_PROPERTIES;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jdom2.Document;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class XMLVelocityMerger {

    /**
     * The name of the xml document, added to the context.
     */
    public static final String CONTEXT_DOCUMENT = "document";

    public static final String CONTEXT_PROPERTY_OBJECT = "velocity.context.object";

    protected final VelocityContext context = new VelocityContext();

    protected final VelocityEngine ve;

    @Inject
    public XMLVelocityMerger(@Named(MERGER_PROPERTIES) Properties properties) {
        ve = new VelocityEngine(properties);
        ve.init();

        Map<String, String> keyAndClasses = getClasses(properties);
        for (Entry<String, String> value : keyAndClasses.entrySet()) {
            try {
                Class<?> clazz = Class.forName(value.getValue());
                context.put(value.getKey(), clazz.newInstance());
            } catch (Exception e) {
                System.err.println("Couldn't instantiate class:"
                        + value.getValue() + " with key:" + value.getKey()
                        + " and put it in Velocity context:" + e.toString());
            }

        }

        final Enumeration<?> e = properties.propertyNames();

        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            context.put(key, properties.getProperty(key));
        }
    }

    public void merge(String template, Document doc, Writer out)
            throws IOException {
        context.put(CONTEXT_DOCUMENT, doc);
        final Template fromTemplate = ve.getTemplate(template);
        fromTemplate.merge(context, out);
    }

    private Map<String, String> getClasses(Properties properties) {

        Map<String, String> keyAndClass = new HashMap<String, String>();
        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys) {
            if (key.startsWith(CONTEXT_PROPERTY_OBJECT)) {
                String value = properties.getProperty(key);
                String className = value.substring(0, value.indexOf(":"));
                String contextKey = value.substring(value.indexOf(":") + 1,
                        value.length());
                keyAndClass.put(contextKey, className);
            }
        }
        return keyAndClass;
    }
}
