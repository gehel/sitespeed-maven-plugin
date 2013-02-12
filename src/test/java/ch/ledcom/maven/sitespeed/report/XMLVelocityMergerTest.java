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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

public class XMLVelocityMergerTest {

    private static final String TEMPLATE_NAME = "/report/velocity/test-template.vm";

    private XMLVelocityMerger merger;

    @Before
    public void setUp() {
        Properties props = new Properties();
        props.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
        props.put("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        merger = new XMLVelocityMerger(props);
    }

    @Test
    public void generateDummyReport() throws IOException,
            ParserConfigurationException {
        StringWriter out = new StringWriter();
        merger.merge(TEMPLATE_NAME, createTestDocument(), out);
        assertEquals("Output from template merge is not as expected.",
                "\n\nsub-template-test\n", out.toString());
    }

    private Document createTestDocument() {
        return new Document(new Element("test"));
    }
}
