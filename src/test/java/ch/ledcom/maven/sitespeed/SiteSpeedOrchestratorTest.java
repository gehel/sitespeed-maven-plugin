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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ch.ledcom.maven.sitespeed.analyzer.SiteSpeedAnalyzer;
import ch.ledcom.maven.sitespeed.crawler.SiteSpeedCrawler;
import ch.ledcom.maven.sitespeed.crawler.URICallback;
import ch.ledcom.maven.sitespeed.report.SiteSpeedReporter;

import com.google.common.io.Files;

public class SiteSpeedOrchestratorTest {

    private static final int URI_COUNT = 1000;

    private static final URI uri1 = safeURI("http://test1.com");
    private static final URI uri2 = safeURI("http://test2.com");
    private static final URI uri3 = safeURI("http://test3.com");

    private static final Document doc1 = createDocument("test1");
    private static final Document doc2 = createDocument("test2");
    private static final Document doc3 = createDocument("test3");

    private SiteSpeedCrawler crawler;
    private SiteSpeedAnalyzer analyzer;
    private SiteSpeedReporter reporter;

    private SiteSpeedOrchestrator orchestrator;

    private ExecutorService analyzerService;

    private ExecutorService reportService;

    @Before
    public void setUp() throws IOException {
        crawler = mock(SiteSpeedCrawler.class);
        analyzer = mock(SiteSpeedAnalyzer.class);
        reporter = mock(SiteSpeedReporter.class);

        analyzerService = createExecutorService();
        reportService = createExecutorService();
        orchestrator = new SiteSpeedOrchestrator(crawler, analyzer, reporter,
                analyzerService, reportService, getOutputDir(),
                new SystemStreamLog());

    }

    @Test
    public void objectsGoThroughAllOrchestratorStep()
            throws MalformedURLException, IOException, JDOMException,
            InterruptedException {

        when(crawler.crawl(any(URICallback.class))).then(submitTestUris());

        when(analyzer.analyze(uri1.toURL())).thenReturn(doc1);
        when(analyzer.analyze(uri2.toURL())).thenReturn(doc2);
        when(analyzer.analyze(uri3.toURL())).thenReturn(doc3);

        orchestrator.siteSpeed();

        verify(reporter, atLeast(URI_COUNT / 3)).report(eq(doc1),
                any(Writer.class));
        verify(reporter, atLeast(URI_COUNT / 3)).report(eq(doc2),
                any(Writer.class));
        verify(reporter, atLeast(URI_COUNT / 3)).report(eq(doc3),
                any(Writer.class));
    }

    private Answer<?> submitTestUris() {
        return new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                URICallback callback = (URICallback) invocation.getArguments()[0];
                for (int i = 0; i < URI_COUNT; i++) {
                    URI uri = null;
                    switch (i % 3) {
                    case 0:
                        uri = uri1;
                        break;
                    case 1:
                        uri = uri2;
                        break;
                    case 2:
                        uri = uri3;
                        break;
                    default:
                        break;
                    }
                    callback.submit(uri);
                }
                return null;
            }

        };
    }

    @After
    public void shutdown() {
        analyzerService.shutdown();
        reportService.shutdown();
    }

    private ExecutorService createExecutorService() {
        return new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1), new CallerRunsPolicy());
    }

    private static URI safeURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
        }
        return null;
    }

    private static Document createDocument(String content) {
        return new Document(new Element(content));
    }

    private File getOutputDir() throws IOException {
        File temp = Files.createTempDir();
        temp.deleteOnExit();
        return temp;
    }

}
