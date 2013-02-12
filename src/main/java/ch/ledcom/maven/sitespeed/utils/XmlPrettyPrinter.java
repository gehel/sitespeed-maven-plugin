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
package ch.ledcom.maven.sitespeed.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Node;

public final class XmlPrettyPrinter {
    private XmlPrettyPrinter() {
    }

    public static void prettyPrint(Document doc, OutputStream out)
            throws IOException {
        new XMLOutputter(Format.getPrettyFormat()).output(doc, out);
    }

    public static String prettyPrint(Node node)
            throws TransformerFactoryConfigurationError, TransformerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        prettyPrint(node, out);
        return out.toString();
    }

    public static String prettyPrint(Document doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        prettyPrint(doc, out);
        return out.toString();
    }

    public static void prettyPrint(Node node, OutputStream out)
            throws TransformerFactoryConfigurationError, TransformerException {
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        Source source = new DOMSource(node);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }
}
