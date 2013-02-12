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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.ledcom.maven.sitespeed.analyzer.PageSpeedAnalyzerTest;
import ch.ledcom.maven.sitespeed.crawler.SiteSpeedCrawlerTest;
import ch.ledcom.maven.sitespeed.guice.SiteSpeedModuleTest;
import ch.ledcom.maven.sitespeed.report.SiteSpeedReporterTest;
import ch.ledcom.maven.sitespeed.report.XMLVelocityMergerTest;

@RunWith(Suite.class)
@SuiteClasses({ SiteSpeedOrchestratorTest.class, PageSpeedAnalyzerTest.class,
        SiteSpeedCrawlerTest.class, SiteSpeedModuleTest.class,
        SiteSpeedReporterTest.class, XMLVelocityMergerTest.class,
        FullIntegrationTest.class })
public class AllTests {
}
