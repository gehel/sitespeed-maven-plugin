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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.logging.Log;

import ch.ledcom.maven.sitespeed.Configuration;
import ch.ledcom.maven.sitespeed.SiteSpeedOrchestrator;
import ch.ledcom.maven.sitespeed.SiteSpeedSingleThreadedOrchestrator;
import ch.ledcom.maven.sitespeed.analyzer.SiteSpeedAnalyzer;
import ch.ledcom.maven.sitespeed.crawler.SiteSpeedCrawler;
import ch.ledcom.maven.sitespeed.report.SiteSpeedReporter;
import ch.ledcom.maven.sitespeed.report.XMLVelocityMerger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class SiteSpeedModule extends AbstractModule {

    private final File phantomJS;
    private final boolean verifyUrl;
    private final int level;
    private final String followPath;
    private final String noFollowPath;
    private final String proxyHost;
    private final String proxyType;
    private final String requestHeaders;
    private final String ruleset;
    private final String template;
    private final String userAgent;
    private final String viewPort;
    private final URL startUrl;
    private final Properties mergerProperties;
    private final File outputDir;
    private final Log log;

    public SiteSpeedModule(File phantomJS, boolean verifyUrl, int level,
            String followPath, String noFollowPath, String proxyHost,
            String proxyType, String requestHeaders, String ruleset,
            String template, String userAgent, String viewPort, URL startUrl,
            Properties mergerProperties, File outputDir, Log log) {
        this.phantomJS = phantomJS;
        this.verifyUrl = verifyUrl;
        this.level = level;
        this.followPath = followPath;
        this.noFollowPath = noFollowPath;
        this.proxyHost = proxyHost;
        this.proxyType = proxyType;
        this.requestHeaders = requestHeaders;
        this.ruleset = ruleset;
        this.template = template;
        this.userAgent = userAgent;
        this.viewPort = viewPort;
        this.startUrl = startUrl;
        this.mergerProperties = mergerProperties;
        this.outputDir = outputDir;
        this.log = log;
    }

    @Override
    protected void configure() {
        bind(SiteSpeedOrchestrator.class).in(Singleton.class);
        bind(SiteSpeedSingleThreadedOrchestrator.class).in(Singleton.class);
        bind(SiteSpeedCrawler.class).in(Singleton.class);
        bind(SiteSpeedAnalyzer.class).in(Singleton.class);
        bind(SiteSpeedReporter.class).in(Singleton.class);
        bind(XMLVelocityMerger.class).in(Singleton.class);
    }

    @Provides
    @Named(Configuration.PHANTOM_JS)
    public File getPhantomJS() {
        return phantomJS;
    }

    @Provides
    @Named(Configuration.VERIFY_URL)
    public boolean isVerifyUrl() {
        return verifyUrl;
    }

    @Provides
    @Named(Configuration.LEVEL)
    public int getLevel() {
        return level;
    }

    @Provides
    @Named(Configuration.FOLLOW_PATH)
    public String getFollowPath() {
        return followPath;
    }

    @Provides
    @Named(Configuration.NO_FOLLOW_PATH)
    public String getNoFollowPath() {
        return noFollowPath;
    }

    @Provides
    @Named(Configuration.PROXY_HOST)
    public String getProxyHost() {
        return proxyHost;
    }

    @Provides
    @Named(Configuration.PROXY_TYPE)
    public String getProxyType() {
        return proxyType;
    }

    @Provides
    @Named(Configuration.REQUEST_HEADERS)
    public String getRequestHeaders() {
        return requestHeaders;
    }

    @Provides
    @Named(Configuration.RULESET)
    public String getRuleset() {
        return ruleset;
    }

    @Provides
    @Named(Configuration.TEMPLATE)
    public String getTemplate() {
        return template;
    }

    @Provides
    @Named(Configuration.USER_AGENT)
    public String getUserAgent() {
        return userAgent;
    }

    @Provides
    @Named(Configuration.VIEWPORT)
    public String getViewPort() {
        return viewPort;
    }

    @Provides
    @Named(Configuration.START_URL)
    public URL getStartUrl() {
        return startUrl;
    }

    @Provides
    @Named(Configuration.MERGER_PROPERTIES)
    public Properties getMergerProperties() {
        return mergerProperties;
    }

    @Provides
    @Named(Configuration.OUTPUT_DIR)
    public File getOutputDir() {
        return outputDir;
    }

    @Provides
    @Named(Configuration.ANALYZER_SERVICE)
    public ExecutorService getAnalyzerService() {
        return new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1), new CallerRunsPolicy());
    }

    @Provides
    @Named(Configuration.REPORT_SERVICE)
    public ExecutorService getReportService() {
        return new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1), new CallerRunsPolicy());
    }

    @Provides
    public Log getLog() {
        return log;
    }
}
