/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.fe.wiring;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Renders what a web.xml registers, independent of how it is written down: declarations are sorted, while
 * listeners, filter mappings and welcome files keep document order, because the container applies them in that
 * order. Multi-valued init-params (Jersey package and provider lists) are split into one value per line.
 */
final class WebXmlSnapshot {

    private WebXmlSnapshot() {
    }

    static String render(File webXml) throws Exception {
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(webXml).getDocumentElement();
        List<String> lines = new ArrayList<>();
        children(root, "context-param").stream()
            .sorted(Comparator.comparing(param -> text(param, "param-name")))
            .forEach(param -> lines.add("context-param " + text(param, "param-name") + " = " + text(param, "param-value")));
        children(root, "listener").forEach(listener -> lines.add("listener " + text(listener, "listener-class")));
        children(root, "filter").stream()
            .sorted(Comparator.comparing(filter -> text(filter, "filter-name")))
            .forEach(filter -> renderComponent(lines, "filter", filter, "filter-name", "filter-class"));
        for (Element mapping : children(root, "filter-mapping")) {
            lines.add("filter-mapping " + text(mapping, "filter-name"));
            renderMappingTargets(lines, mapping, "filter-name");
        }
        children(root, "servlet").stream()
            .sorted(Comparator.comparing(servlet -> text(servlet, "servlet-name")))
            .forEach(servlet -> renderComponent(lines, "servlet", servlet, "servlet-name", "servlet-class"));
        children(root, "servlet-mapping").stream()
            .sorted(Comparator.comparing(mapping -> text(mapping, "servlet-name")))
            .forEach(mapping -> {
                lines.add("servlet-mapping " + text(mapping, "servlet-name"));
                renderMappingTargets(lines, mapping, "servlet-name");
            });
        for (Element errorPage : children(root, "error-page")) {
            String trigger = children(errorPage, "exception-type").isEmpty() ? text(errorPage, "error-code") : text(errorPage, "exception-type");
            lines.add("error-page " + trigger + " -> " + text(errorPage, "location"));
        }
        for (Element welcomeFiles : children(root, "welcome-file-list")) {
            children(welcomeFiles, "welcome-file").forEach(file -> lines.add("welcome-file " + file.getTextContent().trim()));
        }
        return lines.stream().collect(Collectors.joining("\n", "", "\n"));
    }

    private static void renderComponent(List<String> lines, String kind, Element component, String nameTag, String classTag) {
        StringBuilder line = new StringBuilder(kind).append(' ').append(text(component, nameTag)).append(' ').append(text(component, classTag));
        if (!children(component, "load-on-startup").isEmpty()) {
            line.append(" load-on-startup=").append(text(component, "load-on-startup"));
        }
        if (!children(component, "async-supported").isEmpty()) {
            line.append(" async-supported=").append(text(component, "async-supported"));
        }
        lines.add(line.toString());
        children(component, "init-param").stream()
            .sorted(Comparator.comparing(param -> text(param, "param-name")))
            .forEach(param -> {
                List<String> values = Arrays.stream(text(param, "param-value").split("[,\\s]+"))
                    .filter(value -> !value.isEmpty()).collect(Collectors.toList());
                if (values.size() == 1) {
                    lines.add("  init-param " + text(param, "param-name") + " = " + values.get(0));
                } else {
                    lines.add("  init-param " + text(param, "param-name") + " =");
                    values.forEach(value -> lines.add("    " + value));
                }
            });
    }

    private static void renderMappingTargets(List<String> lines, Element mapping, String nameTag) {
        NodeList nodes = mapping.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element && !nameTag.equals(node.getNodeName())) {
                lines.add("  " + node.getNodeName() + " " + node.getTextContent().trim());
            }
        }
    }

    private static List<Element> children(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element && tag.equals(nodes.item(i).getNodeName())) {
                result.add((Element) nodes.item(i));
            }
        }
        return result;
    }

    private static String text(Element parent, String tag) {
        return children(parent, tag).get(0).getTextContent().trim();
    }
}
