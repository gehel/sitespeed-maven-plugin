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
package ch.ledcom.maven.sitespeed.guice;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ledcom.maven.sitespeed.SiteSpeedOrchestrator;
import ch.ledcom.maven.sitespeed.utils.UrlUtils;

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.soulgalore.crawler.guice.CrawlModule;

public class SiteSpeedModuleTest {

    private static final String PROXY_HOST = null;
    private static final String PROXY_TYPE = null;
    private static final String NO_FOLLOW_PATH = "";
    private static final String FOLLOW_PATH = "/";
    private static final File PHANTOM_JS = new File("");
    private static final File YSLOW = new File("");
    private static final boolean VERIFY_URL = false;
    private static final int LEVEL = 1;
    private static final String REQUEST_HEADERS = null;
    private static final String RULESET = "";
    private static final String TEMPLATE = "page";
    private static final String USER_AGENT = null;
    private static final String VIEW_PORT = null;
    private static final URL START_URL = UrlUtils.safeUrl("http://test.com/");
    private static final Properties MERGER_PROPERTIES = new Properties();

    private File outputDir;
    
    @Before
    public void createTempOutputDir() {
        outputDir = Files.createTempDir();
    }
    
    @Test
    public void checkThatWiringIsOk() {
        Injector injector = Guice.createInjector( //
                new SiteSpeedModule( //
                        PHANTOM_JS, //
                        YSLOW, //
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
                        START_URL, //
                        MERGER_PROPERTIES, //
                        outputDir, //
                        new SystemStreamLog()), //
                new CrawlModule());
        SiteSpeedOrchestrator orchestrator = injector
                .getInstance(SiteSpeedOrchestrator.class);
        Assert.assertNotNull("Could not create Orchestrator", orchestrator);
    }

    @After
    public void destroyTempOutputDir() {
        outputDir.delete();
    }
}
