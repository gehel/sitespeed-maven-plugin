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

public final class Configuration {

    private static final String PREFIX = "ch.ledcom.maven.sitespeed.";

    public static final String VIEWPORT = PREFIX + "viewport";
    public static final String USER_AGENT = PREFIX + "userAgent";
    public static final String RULESET = PREFIX + "ruleset";
    public static final String PROXY_TYPE = PREFIX + "proxyType";
    public static final String PROXY_HOST = PREFIX + "proxyHost";
    public static final String YSLOW = PREFIX + "yslow";
    public static final String PHANTOM_JS = PREFIX + "phantomJS";
    public static final String START_URL = PREFIX + "startUrl";
    public static final String REQUEST_HEADERS = PREFIX + "requestHeaders";
    public static final String NO_FOLLOW_PATH = PREFIX + "noFollowPath";
    public static final String FOLLOW_PATH = PREFIX + "followPath";
    public static final String VERIFY_URL = PREFIX + "verifyUrl";
    public static final String LEVEL = PREFIX + "level";
    public static final String TEMPLATE = PREFIX + "template";
    public static final String MERGER_PROPERTIES = PREFIX + "mergerProperties";
    public static final String URL = PREFIX + "url";
    public static final String CRAWL_DEPTH = PREFIX + "crawlDepth";
    public static final String SKIP_URLS = PREFIX + "skipUrls";
    public static final String OUTPUT_DIR = PREFIX + "outputDir";
    public static final String ANALYZER_SERVICE = PREFIX + "analyzerService";
    public static final String REPORT_SERVICE = PREFIX + "reportService";

    private Configuration() {

    }

}
