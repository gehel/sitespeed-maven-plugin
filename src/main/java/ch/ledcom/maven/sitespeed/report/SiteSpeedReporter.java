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
package ch.ledcom.maven.sitespeed.report;

import static ch.ledcom.maven.sitespeed.Configuration.TEMPLATE;

import java.io.IOException;
import java.io.Writer;

import org.jdom2.Document;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SiteSpeedReporter {

    private final XMLVelocityMerger merger;
    private final String template;

    @Inject
    public SiteSpeedReporter(XMLVelocityMerger merger,
            @Named(TEMPLATE) String template) {
        this.merger = merger;
        this.template = template;
    }

    public void report(Document doc, Writer out) throws IOException {
        merger.merge(template, doc, out);
    }

}
