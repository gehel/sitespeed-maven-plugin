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
package ch.ledcom.maven.sitespeed;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.ledcom.maven.sitespeed.guice.SiteSpeedModule;
import ch.ledcom.maven.sitespeed.httpserver.HttpTestServer;
import ch.ledcom.maven.sitespeed.utils.UrlUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.soulgalore.crawler.guice.CrawlModule;

public class FullIntegrationTest {

    private static final String PROXY_HOST = null;
    private static final String PROXY_TYPE = null;
    private static final String NO_FOLLOW_PATH = "";
    private static final String FOLLOW_PATH = "/";
    private static final File PHANTOM_JS = new File(
            "/home/gehel/Downloads/phantomjs-1.8.1-linux-x86_64/bin/phantomjs");
    private static final File YSLOW = new File(
            "/home/gehel/dev/sitespeed.io/sitespeed.io/dependencies/yslow-3.1.4-sitespeed.js");
    private static final boolean VERIFY_URL = false;
    private static final int LEVEL = 1;
    private static final String REQUEST_HEADERS = null;
    private static final String RULESET = "";
    private static final String TEMPLATE = "/report/velocity/page.vm";
    private static final String USER_AGENT = null;
    private static final String VIEW_PORT = null;
    private static final Properties MERGER_PROPERTIES = new Properties();

    private static final int HTTP_PORT = 9099;
    private static final String HTTP_PATH1 = "/test1.html";
    private static final String HTTP_CONTENT1 = "<html><head><title>test title</title></head><body><a href=\"test2.html\"/></body></html>";
    private static final String HTTP_PATH2 = "/test2.html";
    private static final String HTTP_CONTENT2 = "<html><head><title>test title</title></head><body><a href=\"test1.html\"/></body></html>";
    private static final URL HTTP_URL1 = UrlUtils.safeUrl("http://localhost:"
            + HTTP_PORT + HTTP_PATH1);

    private File outputDir;

    private HttpTestServer httpServer;
    private SiteSpeedOrchestrator orchestrator;

    @Before
    public void startHttpServer() throws IOException {
        Map<String, String> pages = ImmutableMap.<String, String> builder() //
                .put(HTTP_PATH1, HTTP_CONTENT1) //
                .put(HTTP_PATH2, HTTP_CONTENT2) //
                .build();
        httpServer = new HttpTestServer(HTTP_PORT, pages);
        httpServer.start();
    }

    @Before
    public void createOrchestrator() throws IOException {
        outputDir = Files.createTempDir();
        System.out.println(outputDir.getAbsolutePath());

        MERGER_PROPERTIES.load(this.getClass().getClassLoader()
                .getResourceAsStream("merger.properties"));

        Injector injector = Guice.createInjector( //
                new SiteSpeedModule( //
                        PHANTOM_JS, //
                        VERIFY_URL, //
                        LEVEL, //
                        FOLLOW_PATH, //
                        NO_FOLLOW_PATH, //
                        PROXY_HOST, //
                        PROXY_TYPE, //
                        REQUEST_HEADERS, //
                        RULESET, //
                        TEMPLATE, //
                        USER_AGENT, //
                        VIEW_PORT, //
                        HTTP_URL1, //
                        MERGER_PROPERTIES, //
                        outputDir, //
                        new SystemStreamLog()), //
                new CrawlModule());
        orchestrator = injector.getInstance(SiteSpeedOrchestrator.class);
    }

    @Test
    public void runSimpleAnalysis() throws IOException {
        orchestrator.siteSpeed();
        assertTrue("No report has been generated.", outputDir.list().length > 0);
    }

    @After
    public void destroyTempOutputDir() {
        // outputDir.delete();
    }

    @After
    public void shutdown() {
        httpServer.stop();
    }

}
