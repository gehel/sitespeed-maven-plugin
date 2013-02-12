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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;

public class SiteSpeedReporterTest {

    private static final String TEMPLATE_NAME = "test-template";

    private SiteSpeedReporter reporter;

    private XMLVelocityMerger merger;

    @Before
    public void setUp() {
        merger = mock(XMLVelocityMerger.class);
        reporter = new SiteSpeedReporter(merger, TEMPLATE_NAME);
    }

    /**
     * Test that parameters are correctly forwarded to merger.
     * 
     * Test is so simple that it is almost useless at the moment. Some more
     * logic will probably be added later, so we already have a minimal skeleton
     * for tests.
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     */
    @Test
    public void tempalteNameIsSentToMerger() throws IOException,
            ParserConfigurationException {
        StringWriter out = new StringWriter();
        Document doc = mock(Document.class);
        reporter.report(doc, out);
        verify(merger).merge(TEMPLATE_NAME, doc, out);
    }

}
