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
package ch.ledcom.maven.sitespeed.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import ch.ledcom.maven.sitespeed.httpserver.HttpTestServer;
import ch.ledcom.maven.sitespeed.utils.UrlUtils;

public class PageSpeedAnalyzerTest {

    private static final String HTTP_CONTENT = "<html><head><title>test title</title></head><body>test body</body></html>";
    private static final String HTTP_PATH = "/test.html";
    private static final int HTTP_PORT = 9099;

    private static final URL HTTP_URL = UrlUtils.safeUrl("http://localhost:"
            + HTTP_PORT + HTTP_PATH);
    private static final URL INVALID_URL = UrlUtils
            .safeUrl("http://non-existing-website.com/toto");

    private HttpTestServer httpServer;

    private String proxyHost = "";
    private String proxyType = "";
    private String ruleset = "sitespeed.io-1.6";
    private String userAgent = "Mozilla/6.0";
    private String viewport = "1280x800";
    private static final File phantomJS = new File(
            "/home/gehel/dev/sitespeed.io/phantomjs-1.8.1-linux-x86_64/bin/phantomjs");
    private File yslow = new File(
            "/home/gehel/dev/sitespeed.io/sitespeed.io/dependencies/yslow-3.1.4-sitespeed.js");
    private SiteSpeedAnalyzer analyzer;

    @Before
    public void setUp() throws IOException {
        analyzer = new SiteSpeedAnalyzer(phantomJS, yslow, proxyHost,
                proxyType, ruleset, userAgent, viewport);
        httpServer = new HttpTestServer(HTTP_PORT, HTTP_PATH, HTTP_CONTENT);
        httpServer.start();
    }

    @Test
    public void analyzeDummyPage() throws IOException, JDOMException,
            InterruptedException {
        Document doc = analyzer.analyze(HTTP_URL);
        assertNotNull("Analyzer returned null document", doc);
        // we only care that there are results, not what they are
        assertEquals("results", doc.getRootElement().getName());
    }

    @Test(expected = IOException.class)
    public void crashIfPhantomJSDoesNotExist() throws IOException,
            JDOMException, InterruptedException {
        analyzer = new SiteSpeedAnalyzer(new File(""), yslow, proxyHost,
                proxyType, ruleset, userAgent, viewport);
        analyzer.analyze(HTTP_URL);
    }

    @Ignore
    @Test(expected = SAXParseException.class)
    public void crashIfYSlowDoesNotExist() throws IOException, JDOMException,
            InterruptedException {
        analyzer = new SiteSpeedAnalyzer(phantomJS, new File(""), proxyHost,
                proxyType, ruleset, userAgent, viewport);
        analyzer.analyze(HTTP_URL);
    }

    // TODO: we might want to encapsulate exception in this case ...
    @Ignore
    @Test(expected = SAXParseException.class)
    public void crashOnNonExistingUrl() throws IOException, JDOMException,
            InterruptedException {
        analyzer.analyze(INVALID_URL);
    }

    @After
    public void shutdown() {
        httpServer.stop();
    }

}
