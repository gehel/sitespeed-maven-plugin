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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import ch.ledcom.maven.sitespeed.analyzer.SiteSpeedAnalyzer;
import ch.ledcom.maven.sitespeed.crawler.SiteSpeedCrawler;
import ch.ledcom.maven.sitespeed.crawler.URICallback;
import ch.ledcom.maven.sitespeed.report.ResourceFiles;
import ch.ledcom.maven.sitespeed.report.SiteSpeedReporter;
import ch.ledcom.maven.sitespeed.utils.XmlPrettyPrinter;

import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SiteSpeedSingleThreadedOrchestrator {

    private final SiteSpeedCrawler crawler;
    private final SiteSpeedAnalyzer analyzer;
    private final SiteSpeedReporter reporter;
    private final File outputDir;

    private final Log log;

    @Inject
    public SiteSpeedSingleThreadedOrchestrator(SiteSpeedCrawler crawler,
            SiteSpeedAnalyzer analyzer, SiteSpeedReporter reporter,

            @Named(Configuration.OUTPUT_DIR) File outputDir, Log log) {
        this.crawler = crawler;
        this.analyzer = analyzer;
        this.reporter = reporter;
        this.outputDir = outputDir;
        this.log = log;
    }

    public void siteSpeed() throws IOException {

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // crawl site to get the list of URLs to analyze
        crawler.crawl(new URICallback() {
            @Override
            public void submit(final URI uri) {
                Writer out = null;
                boolean threw = true;
                try {
                    final URL url = uri.toURL();
                    log.info("Received URL to analyze [" + url.toExternalForm()
                            + "]");
                    Document doc = analyzer.analyze(url);

                    out = createReportWriter(uri);
                    log.info("Creating report for URL [" + url.toExternalForm()
                            + "]");
                    log.info("Document to report for URL ["
                            + url.toExternalForm() + "]");
                    log.debug(XmlPrettyPrinter.prettyPrint(doc));
                    reporter.report(doc, out);
                    threw = false;
                    crawler.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JDOMException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        Closeables.close(out, threw);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        new ResourceFiles().export(outputDir);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Writer createReportWriter(URI uri) throws IOException {
        String filename = uri.getHost() + uri.getPath().replace("/", ".")
                + ".html";
        File out = new File(outputDir, filename);
        return new FileWriter(out);
    }
}
