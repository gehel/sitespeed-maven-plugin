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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;

import ch.ledcom.maven.sitespeed.analyzer.SiteSpeedAnalyzer;
import ch.ledcom.maven.sitespeed.crawler.SiteSpeedCrawler;
import ch.ledcom.maven.sitespeed.crawler.URICallback;
import ch.ledcom.maven.sitespeed.report.ResourceFiles;
import ch.ledcom.maven.sitespeed.report.SiteSpeedReporter;
import ch.ledcom.maven.sitespeed.utils.XmlPrettyPrinter;

import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SiteSpeedOrchestrator {

    private final SiteSpeedCrawler crawler;
    private final SiteSpeedAnalyzer analyzer;
    private final SiteSpeedReporter reporter;
    private final ExecutorService analyzerService;
    private final ExecutorService reportService;
    private final File outputDir;

    private final Log log;

    @Inject
    public SiteSpeedOrchestrator(
            SiteSpeedCrawler crawler,
            SiteSpeedAnalyzer analyzer,
            SiteSpeedReporter reporter,
            @Named(Configuration.ANALYZER_SERVICE) ExecutorService analyzerService,
            @Named(Configuration.REPORT_SERVICE) ExecutorService reportService,
            @Named(Configuration.OUTPUT_DIR) File outputDir, Log log) {
        this.crawler = crawler;
        this.analyzer = analyzer;
        this.reporter = reporter;
        this.analyzerService = analyzerService;
        this.reportService = reportService;
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
                try {
                    final URL url = uri.toURL();
                    final Future<Document> documentFuture = analyzerService
                            .submit(new Callable<Document>() {
                                @Override
                                public Document call() throws Exception {
                                    log.info("Received URL to analyze ["
                                            + url.toExternalForm() + "]");
                                    return analyzer.analyze(url);
                                }
                            });

                    reportService.execute(new Runnable() {
                        @Override
                        public void run() {
                            Writer out = null;
                            boolean threw = true;
                            try {
                                out = createReportWriter(uri);
                                log.info("Creating report for URL ["
                                        + url.toExternalForm() + "]");
                                Document doc = documentFuture.get();
                                log.debug(XmlPrettyPrinter.prettyPrint(doc));
                                reporter.report(doc, out);
                                threw = false;
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
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
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } finally {
                    crawler.shutdown();
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
