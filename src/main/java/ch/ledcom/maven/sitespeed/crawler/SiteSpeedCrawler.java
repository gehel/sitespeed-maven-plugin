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

import static ch.ledcom.maven.sitespeed.Configuration.FOLLOW_PATH;
import static ch.ledcom.maven.sitespeed.Configuration.LEVEL;
import static ch.ledcom.maven.sitespeed.Configuration.NO_FOLLOW_PATH;
import static ch.ledcom.maven.sitespeed.Configuration.REQUEST_HEADERS;
import static ch.ledcom.maven.sitespeed.Configuration.START_URL;
import static ch.ledcom.maven.sitespeed.Configuration.VERIFY_URL;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.soulgalore.crawler.core.Crawler;
import com.soulgalore.crawler.core.CrawlerConfiguration;
import com.soulgalore.crawler.core.CrawlerResult;
import com.soulgalore.crawler.core.PageURL;

public class SiteSpeedCrawler {

    private final int level;
    private final boolean verifyUrl;
    private final String followPath;
    private final String noFollowPath;
    @Nullable
    private final String requestHeaders;
    private final URL startUrl;
    private final Crawler crawler;

    @Inject
    public SiteSpeedCrawler(
            Crawler crawler,
            @Named(LEVEL) int level,
            @Named(VERIFY_URL) boolean verifyUrl,
            @Named(FOLLOW_PATH) String followPath,
            @Named(NO_FOLLOW_PATH) String noFollowPath,
            @Named(REQUEST_HEADERS) @Nullable String requestHeaders,
            @Named(START_URL) URL startUrl) {
        this.crawler = crawler;
        this.level = level;
        this.verifyUrl = verifyUrl;
        this.followPath = followPath;
        this.noFollowPath = noFollowPath;
        this.requestHeaders = requestHeaders;
        this.startUrl = startUrl;
    }

    public List<URI> crawl(final URICallback callback) {
        List<URI> uris = new ArrayList<URI>();
        final CrawlerResult result = crawler.getUrls(getConfiguration());
        Set<PageURL> pageURLs = result.getUrls();
        for (PageURL pageURL : pageURLs) {
            uris.add(pageURL.getUri());
            callback.submit(pageURL.getUri());
        }
        return uris;
    }

    public void shutdown() {
        crawler.shutdown();
    }

    private CrawlerConfiguration getConfiguration() {
        return CrawlerConfiguration.builder() //
                .setMaxLevels(level) //
                .setVerifyUrls(verifyUrl) //
                .setOnlyOnPath(followPath) //
                .setNotOnPath(noFollowPath) //
                .setRequestHeaders(requestHeaders) //
                .setStartUrl(startUrl.toExternalForm()) //
                .build();
    }

}
