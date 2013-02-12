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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;

public class ResourceFiles {

    private static final String BASE_DIR = "report";

    private final List<File> css;
    private final List<File> images;
    private final List<File> js;

    public ResourceFiles() {
        this.css = ImmutableList.<File> builder()//
                .add(new File("css/bootstrap.min.css")) //
                .build();
        this.images = ImmutableList.<File> builder()//
                .add(new File("img/apple-touch-icon-114-precomposed.png")) //
                .add(new File("img/glyphicons-halflings.png")) //
                .add(new File("img/apple-touch-icon-144-precomposed.png")) //
                .add(new File("img/glyphicons-halflings-white.png")) //
                .add(new File("img/apple-touch-icon-72-precomposed.png")) //
                .add(new File("img/sitespeed-logo.gif")) //
                .add(new File("img/favicon.ico")) //
                .build();
        this.js = ImmutableList.<File> builder()//
                .add(new File("js/bootstrap.min.js")) //
                .add(new File("js/jquery.tablesorter.min.js")) //
                .add(new File("js/jquery-1.8.3.min.js")) //
                .add(new File("js/stupidtable.min.js")) //
                .build();
    }

    public void export(File target) throws IOException {
        concatenate(css, new File(target, "css/styles.css"));
        export(images, target);
        concatenate(js, new File(target, "js/all.js"));
    }

    private void concatenate(List<File> files, File target) throws IOException {
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        InputStream in = null;
        OutputStream out = new FileOutputStream(target);
        boolean threw = true;
        try {
            for (File file : files) {
                boolean threw2 = true;
                try {
                    in = this
                            .getClass()
                            .getClassLoader()
                            .getResourceAsStream(
                                    BASE_DIR + "/" + file.getPath());
                    IOUtil.copy(in, out);
                } finally {
                    Closeables.close(in, threw2);
                }
            }
        } finally {
            Closeables.close(out, threw);
        }
    }

    private void export(List<File> files, File target) throws IOException {
        for (File resource : files) {
            export(resource, target);
        }
    }

    private void export(File resource, File targetDir) throws IOException {
        File target = new File(targetDir, resource.getPath());
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        InputStream in = null;
        OutputStream out = null;
        boolean threw = true;
        try {
            in = this.getClass().getClassLoader()
                    .getResourceAsStream(BASE_DIR + "/" + resource.getPath());
            out = new FileOutputStream(target);
            IOUtil.copy(in, out);
            threw = false;
        } finally {
            Closeables.close(in, true);
            Closeables.close(out, threw);
        }
    }

}
