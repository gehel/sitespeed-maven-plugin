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
package ch.ledcom.maven.sitespeed.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

// com.sun classes are restricted, but for a unit test, we dont care
@SuppressWarnings("restriction")
public class HttpTestServer {

    private final HttpServer server;

    public HttpTestServer(final int port, final Map<String, String> pages)
            throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        for (final Entry<String, String> page : pages.entrySet()) {
            server.createContext(page.getKey(), new HttpHandler() {
                @Override
                public void handle(HttpExchange t) throws IOException {
                    t.sendResponseHeaders(200, page.getValue().length());
                    OutputStream out = null;
                    boolean threw = true;
                    try {
                        out = t.getResponseBody();
                        out.write(page.getValue().getBytes());
                        threw = false;
                    } finally {
                        Closeables.close(out, threw);
                    }
                }
            });
        }
        
        server.setExecutor((Executor) null);
    }

    public HttpTestServer(final int port, final String path,
            final String content) throws IOException {
        this(port, ImmutableMap.<String, String> builder().put(path, content)
                .build());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

}
