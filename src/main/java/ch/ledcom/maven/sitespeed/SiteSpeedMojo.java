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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import ch.ledcom.maven.sitespeed.guice.SiteSpeedModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.soulgalore.crawler.guice.CrawlModule;

/**
 * Generate a SiteSpeed.io report.
 * 
 * At the moment this Mojo is a simple wrapper around the sitespeed.io bash
 * script.
 * 
 * @author gehel
 */
@Mojo(name = "sitespeed")
public class SiteSpeedMojo extends AbstractMojo {

    private static final String PROPERTY_PREFIX = "siteSpeed";

    /** Path to the PhantomJS binary. */
    @Parameter(property = Configuration.PHANTOM_JS, required = true)
    private File phantomJSPath;

    /** The start url for the test. */
    @Parameter(property = Configuration.URL, required = true)
    private URL url;

    /** Crawl depth, default is 1. */
    @Parameter(property = Configuration.CRAWL_DEPTH, required = false, defaultValue = "1")
    private int crawlDepth;

    /** Skip urls that contains this in the path. */
    @Parameter(property = Configuration.SKIP_URLS, required = false, defaultValue = "")
    private String skipUrls;

    /** The number of processes that will analyze pages. */
    @Parameter(property = PROPERTY_PREFIX + ".nbProcesses", required = false, defaultValue = "5")
    private int nbProcesses;

    /** The memory heap size for the java applications. */
    @Parameter(property = PROPERTY_PREFIX + ".maxHeap", required = false, defaultValue = "1024")
    private int maxHeap;

    /** The output format, always output as html but you can add images (img). */
    @Parameter(property = PROPERTY_PREFIX + ".outputFormat", required = false)
    private String outputFormat;

    /** The result base directory. */
    @Parameter(property = PROPERTY_PREFIX + ".outputDir", required = false, defaultValue = "${project.build.directory}/sitespeed-result")
    private File outputDir;

    /** Create a tar zip file of the result files. */
    @Parameter(property = PROPERTY_PREFIX + ".zip", required = false, defaultValue = "false")
    private boolean zip;

    /** The proxy host & protocol: proxy.soulgalore.com:80. */
    @Parameter(property = PROPERTY_PREFIX + ".proxy", required = false)
    private String proxy;

    /** The proxy type. */
    @Parameter(property = PROPERTY_PREFIX + ".proxyType", required = false, defaultValue = "http")
    private String proxyType;

    /** The user agent. */
    @Parameter(property = PROPERTY_PREFIX + ".userAgent", required = false, defaultValue = "Mozilla/6.0")
    private String userAgent;

    /** The view port, the page viewport size WidthxHeight, like 400x300. */
    @Parameter(property = PROPERTY_PREFIX + ".viewPort", required = false, defaultValue = "1280x800")
    private String viewport;

    /** The compiled yslow file. */
    @Parameter(property = PROPERTY_PREFIX + ".yslowFile", required = false, defaultValue = "dependencies/yslow-3.1.4-sitespeed.js")
    private File yslowFile;

    /** Which ruleset to use, default is the latest sitespeed.io version. */
    @Parameter(property = PROPERTY_PREFIX + ".rulesetVersion", required = false, defaultValue = "")
    private String rulesetVersion;

    /** Verify URLs ? */
    @Parameter(property = PROPERTY_PREFIX + ".verifyUrl", required = false, defaultValue = "false")
    private boolean verifyUrl;

    /**
     * Main Mojo method.
     * 
     * @throws MojoExecutionException
     *             in case of execution error
     * @throws MojoFailureException
     *             in case of execution failure
     */
    @Override
    public final void execute() throws MojoExecutionException,
            MojoFailureException {
        logParameters();

        Properties mergerProperties = new Properties();
        try {
            mergerProperties.load(this.getClass().getClassLoader()
                    .getResourceAsStream("merger.properties"));

            Injector injector = Guice.createInjector( //
                    new SiteSpeedModule( //
                            phantomJSPath, //
                            yslowFile, //
                            verifyUrl, //
                            crawlDepth, //
                            "", //
                            "", //
                            proxy, //
                            proxyType, //
                            "", //
                            "", //
                            "/report/velocity/page.vm", //
                            userAgent, //
                            viewport, //
                            url, //
                            mergerProperties, //
                            outputDir, //
                            getLog()), //
                    new CrawlModule());
            SiteSpeedOrchestrator orchestrator = injector
                    .getInstance(SiteSpeedOrchestrator.class);
            orchestrator.siteSpeed();
        } catch (IOException e) {
            throw new MojoExecutionException("Could not execute sitespeed.", e);
        }
    }

    private void logParameters() {
        getLog().info("phantomJSPath=[" + phantomJSPath + "]");
        getLog().info("url=[" + url.toExternalForm() + "]");
        getLog().info("crawlDepth=[" + crawlDepth + "]");
        getLog().info("skipUrls=[" + skipUrls + "]");
        getLog().info("nbProcesses=[" + nbProcesses + "]");
        getLog().info("maxHeap=[" + maxHeap + "]");
        getLog().info("outputFormat=[" + outputFormat + "]");
        getLog().info("outputDir=[" + outputDir + "]");
        getLog().info("zip=[" + zip + "]");
        getLog().info("proxy=[" + proxy + "]");
        getLog().info("proxyType=[" + proxyType + "]");
        getLog().info("userAgent=[" + userAgent + "]");
        getLog().info("viewport=[" + viewport + "]");
        getLog().info("yslowFile=[" + yslowFile + "]");
        getLog().info("rulesetVersion=[" + rulesetVersion + "]");
    }

}
