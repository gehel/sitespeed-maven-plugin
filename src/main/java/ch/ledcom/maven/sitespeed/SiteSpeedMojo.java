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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;

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

    /** Path to the SiteSpeed.io directory. */
    @Parameter(property = PROPERTY_PREFIX + ".siteSpeedPath", required = true)
    private File siteSpeedPath;

    /** Path to the PhantomJS directory. */
    @Parameter(property = PROPERTY_PREFIX + ".phantomJSPath", required = true)
    private File phantomJSPath;

    /** The start url for the test. */
    @Parameter(property = PROPERTY_PREFIX + ".url", required = true)
    private URL url;

    /** Crawl depth, default is 1. */
    @Parameter(property = PROPERTY_PREFIX + ".crawlDepth", required = false, defaultValue = "1")
    private int crawlDepth;

    /** Skip urls that contains this in the path. */
    @Parameter(property = PROPERTY_PREFIX + ".skipUrls", required = false)
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
    private String yslowFile;

    /** Which ruleset to use, default is the latest sitespeed.io version. */
    @Parameter(property = PROPERTY_PREFIX + ".rulesetVersion", required = false)
    private String rulesetVersion;

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
        BufferedReader reader = null;
        boolean threw = true;
        try {
            ProcessBuilder pb = new ProcessBuilder(constructCmdarray());
            pb.directory(siteSpeedPath);
            enrichEnvironment(pb);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                getLog().info(line);
            }
            int status = p.waitFor();
            if (status != 0) {
                throw new MojoExecutionException(
                        "sitespeed.io exited with status [" + status + "]");
            }
            threw = false;
        } catch (IOException ioe) {
            throw new MojoExecutionException(
                    "IOException when generating report", ioe);
        } catch (InterruptedException ie) {
            throw new MojoExecutionException(
                    "InterruptedException when generating report", ie);
        } finally {
            try {
                Closeables.close(reader, threw);
            } catch (IOException ioe) {
                throw new MojoExecutionException(
                        "IOException when closing process input stream", ioe);
            }
        }
    }

    private void logParameters() {
        getLog().info("siteSpeedPath=[" + siteSpeedPath + "]");
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

    private void enrichEnvironment(ProcessBuilder pb) {
        final String currentPath = pb.environment().get("PATH");
        pb.environment().put("PATH", getPhantomJSBinDir() + ":" + currentPath);
    }

    /**
     * Construct the sitespeed.io command.
     * 
     * @return the command to run (including arguments)
     */
    private List<String> constructCmdarray() {
        ImmutableList.Builder<String> builder = ImmutableList
                .<String> builder();
        builder.add(getSiteSpeedBin().toString());
        builder.add("-u").add(url.toExternalForm());
        builder.add("-d").add(Integer.toString(crawlDepth));
        if (!Strings.isNullOrEmpty(skipUrls)) {
            builder.add("-f").add(skipUrls);
        }
        builder.add("-p").add(Integer.toString(nbProcesses));
        builder.add("-m").add(Integer.toString(maxHeap));
        if (!Strings.isNullOrEmpty(outputFormat)) {
            builder.add("-o").add(outputFormat);
        }
        builder.add("-r").add(outputDir.toString());
        if (zip) {
            builder.add("-z");
        }
        if (!Strings.isNullOrEmpty(proxy)) {
            builder.add("-x").add(proxy);
        }
        if (!Strings.isNullOrEmpty(proxyType)) {
            builder.add("-t").add(proxyType);
        }
        builder.add("-a").add(userAgent);
        builder.add("-v").add(viewport);
        builder.add("-y").add(yslowFile);
        if (!Strings.isNullOrEmpty(rulesetVersion)) {
            builder.add("-l").add(rulesetVersion);
        }
        return builder.build();
    }

    /**
     * Get the <code>bin</code> directory of the PhantomJS distribution.
     * 
     * @return the PhantomJS <code>bin</code> directory
     */
    private File getPhantomJSBinDir() {
        return new File(phantomJSPath, "bin");
    }

    /**
     * Get the <code>sitespeed.io</code> script.
     * 
     * @return the <code>sitespeed.io</code> script
     */
    private File getSiteSpeedBin() {
        return new File(siteSpeedPath, "sitespeed.io");
    }
}
