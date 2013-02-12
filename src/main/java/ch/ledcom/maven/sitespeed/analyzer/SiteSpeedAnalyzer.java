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
package ch.ledcom.maven.sitespeed.analyzer;

import static ch.ledcom.maven.sitespeed.Configuration.PHANTOM_JS;
import static ch.ledcom.maven.sitespeed.Configuration.PROXY_HOST;
import static ch.ledcom.maven.sitespeed.Configuration.PROXY_TYPE;
import static ch.ledcom.maven.sitespeed.Configuration.RULESET;
import static ch.ledcom.maven.sitespeed.Configuration.USER_AGENT;
import static ch.ledcom.maven.sitespeed.Configuration.VIEWPORT;
import static ch.ledcom.maven.sitespeed.Configuration.YSLOW;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.annotation.Nullable;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SiteSpeedAnalyzer {

    private final File phantomJS;
    private final File yslow;
    @Nullable
    private final String proxyHost;
    @Nullable
    private final String proxyType;
    private final String ruleset;
    @Nullable
    private final String userAgent;
    @Nullable
    private final String viewport;
    private final ImmutableList<String> baseCommand;

    // TODO: would be better to use a pool
    private final ThreadLocal<SAXBuilder> docBuilder = new ThreadLocal<SAXBuilder>() {
        @Override
        protected SAXBuilder initialValue() {
            return new SAXBuilder(new XMLReaderSAX2Factory(false));
        }
    };

    @Inject
    public SiteSpeedAnalyzer(@Named(PHANTOM_JS) File phantomJS,
            @Named(YSLOW) File yslow,
            @Named(PROXY_HOST) @Nullable String proxyHost,
            @Named(PROXY_TYPE) @Nullable String proxyType,
            @Named(RULESET) String ruleset,
            @Named(USER_AGENT) @Nullable String userAgent,
            @Named(VIEWPORT) @Nullable String viewport) {
        Preconditions.checkNotNull(phantomJS,
                "Path to PhantomJS cannot be null");
        Preconditions.checkNotNull(yslow, "Path to yslow cannot be null");
        this.phantomJS = phantomJS;
        this.yslow = yslow;
        this.proxyHost = proxyHost;
        this.proxyType = proxyType;
        this.ruleset = ruleset;
        this.userAgent = userAgent;
        this.viewport = viewport;
        this.baseCommand = constructBaseCommand();
    }

    public Document analyze(URL url) throws IOException, JDOMException,
            InterruptedException {
        InputStream in = null;
        boolean threw = true;
        try {
            ProcessBuilder pb = new ProcessBuilder(constructCommand(url));
            Process p = pb.start();
            // FIXME: we need to filter the InputStream as it seems we can get
            // content outside of the XML document (see sitespeed.io)
            in = p.getInputStream();
            Document doc = docBuilder.get().build(in);
            int status = p.waitFor();
            if (status != 0) {
                throw new RuntimeException("PhantomJS returned with status ["
                        + status + "]");
            }
            threw = false;
            return doc;
        } finally {
            Closeables.close(in, threw);
        }
    }

    private ImmutableList<String> constructBaseCommand() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add(phantomJS.getAbsolutePath());
        if (!Strings.isNullOrEmpty(proxyHost)) {
            builder.add("--proxy=" + proxyHost);
        }
        if (!Strings.isNullOrEmpty(proxyType)) {
            builder.add("--proxy-type=" + proxyType);
        }
        builder.add(yslow.getAbsolutePath()) //
                .add("-d") //
                .add("-r").add(ruleset) //
                .add("-f").add("xml");
        if (!Strings.isNullOrEmpty(userAgent)) {
            builder.add("-ua").add(userAgent);
        }
        if (!Strings.isNullOrEmpty(viewport)) {
            builder.add("-vp").add(viewport);
        }
        return builder.build();
    }

    private ImmutableList<String> constructCommand(URL url) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.addAll(baseCommand);
        builder.add(url.toExternalForm());
        return builder.build();
    }

}
