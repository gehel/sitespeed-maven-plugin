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
package ch.ledcom.maven.sitespeed.crawler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.ledcom.maven.sitespeed.httpserver.HttpTestServer;
import ch.ledcom.maven.sitespeed.utils.UrlUtils;

import com.google.common.collect.ImmutableMap;
import com.soulgalore.crawler.core.Crawler;
import com.soulgalore.crawler.core.HTMLPageResponseFetcher;
import com.soulgalore.crawler.core.Parser;
import com.soulgalore.crawler.core.impl.AhrefParser;
import com.soulgalore.crawler.core.impl.DefaultCrawler;
import com.soulgalore.crawler.core.impl.HttpClientResponseFetcher;
import com.soulgalore.crawler.guice.HttpClientProvider;

public class SiteSpeedCrawlerTest {
    private static final int HTTP_PORT = 9099;
    private static final String HTTP_PATH1 = "/test1.html";
    private static final String HTTP_CONTENT1 = "<html><head><title>test title</title></head><body><a href=\"test2.html\"/></body></html>";
    private static final String HTTP_PATH2 = "/test2.html";
    private static final String HTTP_CONTENT2 = "<html><head><title>test title</title></head><body><a href=\"test1.html\"/></body></html>";
    private static final URL HTTP_URL1 = UrlUtils.safeUrl("http://localhost:"
            + HTTP_PORT + HTTP_PATH1);
    private static final URL HTTP_URL2 = UrlUtils.safeUrl("http://localhost:"
            + HTTP_PORT + HTTP_PATH2);

    private HttpTestServer httpServer;
    private SiteSpeedCrawler ssCrawler;

    @Before
    public void startHttpServer() throws IOException {
        Map<String, String> pages = ImmutableMap.<String, String> builder()
                .put(HTTP_PATH1, HTTP_CONTENT1).put(HTTP_PATH2, HTTP_CONTENT2)
                .build();
        httpServer = new HttpTestServer(HTTP_PORT, pages);
        httpServer.start();
    }

    @Before
    public void setUp() {
        HttpClient httpClient = new HttpClientProvider(5, 5000, 5000,
                (String) null, "").get();
        HTMLPageResponseFetcher responseFetcher = new HttpClientResponseFetcher(
                httpClient);
        ExecutorService service = Executors.newFixedThreadPool(2);
        Parser parser = new AhrefParser();
        Crawler crawler = new DefaultCrawler(responseFetcher, service, parser);
        ssCrawler = new SiteSpeedCrawler(crawler, 2, true, "/", "", "",
                HTTP_URL1);
    }

    @Test
    public void crawlTwoPages() throws URISyntaxException {
        URICallback callback = mock(URICallback.class);
        ssCrawler.crawl(callback);

        verify(callback).submit(HTTP_URL1.toURI());
        verify(callback).submit(HTTP_URL2.toURI());
    }

    @After
    public void shutdownHttpServer() {
        httpServer.stop();
    }
}
